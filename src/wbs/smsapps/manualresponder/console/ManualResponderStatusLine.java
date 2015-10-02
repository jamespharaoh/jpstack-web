package wbs.smsapps.manualresponder.console;

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

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.console.part.PagePart;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.misc.CachedGetter;
import wbs.platform.status.console.StatusLine;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.smsapps.manualresponder.model.ManualResponderReportObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderReportRec;

@SingletonComponent ("manualResponderStatusLine")
public 
class ManualResponderStatusLine
	implements StatusLine {

	// dependencies

	@Inject
	Database database;

	@Inject
	ManualResponderReportObjectHelper manualResponderReportHelper;

	@Inject
	ConsoleRequestContext requestContext;

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
			extends CachedGetter<Integer> {
		
			public
			NumTodayCache () {
				super (5000);
			}
		
			@Override
			public
			Integer refresh () {

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

				Integer total = 0;

				List<ManualResponderReportRec> reports =
					manualResponderReportHelper.findByProcessedTime (
						myUser,
						new Interval (
							startOfDay,
							transaction.now ()));

				for (
					ManualResponderReportRec report
						: reports
				) {
					total += report.getNum ();
				}

				return total;

			}

		}

		class NumThisHourCache
			extends CachedGetter<Integer> {
		
			public
			NumThisHourCache () {
				super (5000);
			}
		
			@Override
			public
			Integer refresh () {

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

				Integer total = 0;

				List<ManualResponderReportRec> reports =
					manualResponderReportHelper.findByProcessedTime (
						myUser,
						new Interval (
							startOfHour,
							transaction.now ()));

				for (
					ManualResponderReportRec report
						: reports
				) {
					total += report.getNum ();
				}

				return total;

			}
		
		}

		NumTodayCache numTodayCache =
			new NumTodayCache ();

		NumThisHourCache numThisHourCache =
			new NumThisHourCache ();

	}

}
