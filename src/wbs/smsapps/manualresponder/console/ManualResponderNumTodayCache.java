package wbs.smsapps.manualresponder.console;

import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;

import java.util.List;

import lombok.NonNull;

import org.joda.time.Instant;
import org.joda.time.Interval;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.misc.CachedGetter;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.smsapps.manualresponder.model.ManualResponderOperatorReport;
import wbs.smsapps.manualresponder.model.ManualResponderRequestSearch;

import wbs.utils.time.TextualInterval;

@PrototypeComponent ("manualResponderNumTodayCache")
public
class ManualResponderNumTodayCache
	extends CachedGetter <Long> {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ManualResponderRequestConsoleHelper manualResponderRequestHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// constructors

	public
	ManualResponderNumTodayCache () {

		super (5000l);

	}

	// implementation

	@Override
	public
	Long refresh (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"PerOperatorCaches.NumTodayCache.refresh ()");

		Transaction transaction =
			database.currentTransaction ();

		Instant startOfDay =
			transaction.now ()
				.toDateTime ()
				.toLocalDate ()
				.toDateTimeAtStartOfDay ()
				.toInstant ();

		List <ManualResponderOperatorReport> reports =
			manualResponderRequestHelper.searchOperatorReports (
				taskLogger,
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
