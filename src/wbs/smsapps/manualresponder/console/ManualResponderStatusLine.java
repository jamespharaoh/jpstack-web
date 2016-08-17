package wbs.smsapps.manualresponder.console;

import static wbs.framework.utils.etc.ConcurrentUtils.futureValue;
import static wbs.framework.utils.etc.Misc.isEmpty;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Provider;

import org.joda.time.Instant;
import org.joda.time.Interval;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.console.part.PagePart;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.utils.TextualInterval;
import wbs.platform.misc.CachedGetter;
import wbs.platform.status.console.StatusLine;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;
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
	UserConsoleLogic userConsoleLogic;

	@Inject
	UserObjectHelper userHelper;

	// prototype dependencies

	@Inject
	Provider<ManualResponderStatusLinePart> manualResponderStatusLinePart;

	// state

	Map<Long,PerOperatorCaches> cachesByUserId =
		new HashMap<> ();

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
	Future<String> getUpdateScript () {

		// find or create per-operator caches

		PerOperatorCaches caches =
			cachesByUserId.get (
				userConsoleLogic.userIdRequired ());

		if (caches == null) {

			caches =
				new PerOperatorCaches ()

				.operatorUserId (
					userConsoleLogic.userIdRequired ());

			cachesByUserId.put (
				userConsoleLogic.userIdRequired (),
				caches);

		}

		// return update script

		return futureValue (
			stringFormat (
				"updateManualResponder (%s, %s);\n",
				caches.numTodayCache.get (),
				caches.numThisHourCache.get ()));

	}

	// helpers

	@Accessors (fluent = true)
	class PerOperatorCaches {

		@Getter @Setter
		Long operatorUserId;

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
						userConsoleLogic.userIdRequired ())

					.processedTime (
						TextualInterval.forInterval (
							userConsoleLogic.timezone (),
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
						userConsoleLogic.userIdRequired ())

					.processedTime (
						TextualInterval.forInterval (
							userConsoleLogic.timezone (),
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
