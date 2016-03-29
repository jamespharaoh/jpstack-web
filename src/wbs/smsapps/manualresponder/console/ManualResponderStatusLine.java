package wbs.smsapps.manualresponder.console;

import static wbs.framework.utils.etc.Misc.isEmpty;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Instant;
import org.joda.time.Interval;

import wbs.console.misc.TimeFormatter;
import wbs.console.part.PagePart;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.utils.TextualInterval;
import wbs.platform.misc.CachedGetter;
import wbs.platform.status.console.StatusLine;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.smsapps.manualresponder.model.ManualResponderOperatorReport;
import wbs.smsapps.manualresponder.model.ManualResponderRequestObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderRequestSearch;

@SingletonComponent ("manualResponderStatusLine")
public
class ManualResponderStatusLine
	implements StatusLine {

	// dependencies

	@Inject
	Database database;

	@Inject
	ManualResponderRequestObjectHelper manualResponderRequestHelper;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	TimeFormatter timeFormatter;

	@Inject
	UserObjectHelper userHelper;

	// prototype dependencies

	@Inject
	Provider<ManualResponderStatusLinePart> manualResponderStatusLinePart;

	// state

	Map<Integer,PerOperatorCaches> cachesByUserId =
		new HashMap<Integer,PerOperatorCaches> ();

	// details

	@Override
	public
	String getName () {
		return "manualResponder";
	}

	// implementation

	@Override
	public
	PagePart get () {

		return manualResponderStatusLinePart.get ();

	}

	@Override
	public
	String getUpdateScript () {

		// find or create per-operator caches

		PerOperatorCaches caches =
			cachesByUserId.get (
				requestContext.userId ());

		if (caches == null) {

			caches =
				new PerOperatorCaches ()

				.operatorUserId (
					requestContext.userId ());

			cachesByUserId.put (
				requestContext.userId (),
				caches);

		}

		// return update script

		return stringFormat (
			"updateManualResponder (%s, %s);\n",
			caches.numTodayCache.get (),
			caches.numThisHourCache.get ());

	}

	// helpers

	@Accessors (fluent = true)
	class PerOperatorCaches {

		@Getter @Setter
		Integer operatorUserId;

		class NumTodayCache
			extends CachedGetter<Long> {

			public
			NumTodayCache () {
				super (5000);
			}

			@Override
			public
			Long refresh () {

				Transaction transaction =
					database.currentTransaction ();

				UserRec myUser =
					userHelper.find (
						requestContext.userId ());

				Instant startOfDay =
					transaction.now ()
						.toDateTime ()
						.toLocalDate ()
						.toDateTimeAtStartOfDay ()
						.toInstant ();

				List<ManualResponderOperatorReport> reports =
					manualResponderRequestHelper.searchOperatorReports (
						new ManualResponderRequestSearch ()

					.processedByUserId (
						myUser.getId ())

					.processedTime (
						TextualInterval.forInterval (
							timeFormatter.defaultTimezone (),
							new Interval (
								startOfDay,
								transaction.now ())))

				);

				if (reports.size () > 1) {
					throw new RuntimeException ();
				}

				if (
					isEmpty (
						reports)
				) {

					return 0l;

				} else {

					return reports.get (0).numBilled ();

				}

			}

		}

		class NumThisHourCache
			extends CachedGetter<Long> {

			public
			NumThisHourCache () {
				super (5000);
			}

			@Override
			public
			Long refresh () {

				Transaction transaction =
					database.currentTransaction ();

				UserRec myUser =
					userHelper.find (
						requestContext.userId ());

				Integer hourOfDay =
					transaction.now ()
						.toDateTime ()
						.getHourOfDay ();

				Instant startOfHour =
					transaction.now ()
						.toDateTime ()
						.toLocalDate ()
						.toDateTimeAtStartOfDay ()
						.plusHours (hourOfDay)
						.toInstant ();

				List<ManualResponderOperatorReport> reports =
					manualResponderRequestHelper.searchOperatorReports (
						new ManualResponderRequestSearch ()

					.processedByUserId (
						myUser.getId ())

					.processedTime (
						TextualInterval.forInterval (
							timeFormatter.defaultTimezone (),
							new Interval (
								startOfHour,
								transaction.now ())))

				);

				if (reports.size () > 1) {
					throw new RuntimeException ();
				}

				if (
					isEmpty (
						reports)
				) {

					return 0l;

				} else {

					return reports.get (0).numBilled ();

				}

			}

		}

		NumTodayCache numTodayCache =
			new NumTodayCache ();

		NumThisHourCache numThisHourCache =
			new NumThisHourCache ();

	}

}
