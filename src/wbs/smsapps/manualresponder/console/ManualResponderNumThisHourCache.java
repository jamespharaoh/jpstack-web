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

@PrototypeComponent ("manualResponderNumThisHourCache")
public
class ManualResponderNumThisHourCache
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
	ManualResponderNumThisHourCache () {
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
				"refresh");

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
				taskLogger,
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
