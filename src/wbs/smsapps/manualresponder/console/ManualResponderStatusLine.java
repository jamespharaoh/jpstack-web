package wbs.smsapps.manualresponder.console;

import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.thread.ConcurrentUtils.futureValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.inject.Provider;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Instant;
import org.joda.time.Interval;

import wbs.console.feature.FeatureChecker;
import wbs.console.part.PagePart;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;

import wbs.platform.misc.CachedGetter;
import wbs.platform.status.console.StatusLine;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;

import wbs.smsapps.manualresponder.model.ManualResponderOperatorReport;
import wbs.smsapps.manualresponder.model.ManualResponderRequestObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderRequestSearch;

import wbs.utils.time.TextualInterval;

@SingletonComponent ("manualResponderStatusLine")
public
class ManualResponderStatusLine
	implements StatusLine {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	FeatureChecker featureChecker;

	@SingletonDependency
	ManualResponderRequestObjectHelper manualResponderRequestHelper;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserObjectHelper userHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <ManualResponderStatusLinePart>
	manualResponderStatusLinePartProvider;

	// state

	Map <Long, PerOperatorCaches> cachesByUserId =
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
	PagePart get (
			@NonNull TaskLogger parentTaskLogger) {

		return manualResponderStatusLinePartProvider.get ();

	}

	@Override
	public
	Future <String> getUpdateScript (
			@NonNull TaskLogger parentTaskLogger) {

		if (
			! featureChecker.checkFeatureAccess (
				"queue_items_status_line")
		) {

			return futureValue (
				"updateManualResponder (0, 0);\n");

		}

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
				integerToDecimalString (
					caches.numTodayCache.get ()),
				integerToDecimalString (
					caches.numThisHourCache.get ())));

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
					collectionIsEmpty (
						reports)
				) {

					return 0l;

				} else {

					return reports.get (0).numBilled ();

				}

			}

		}

		class NumThisHourCache
			extends CachedGetter <Long> {

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

				List <ManualResponderOperatorReport> reports =
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
					collectionIsEmpty (
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
