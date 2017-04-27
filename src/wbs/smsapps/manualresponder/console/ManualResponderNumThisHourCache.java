package wbs.smsapps.manualresponder.console;

import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.etc.NullUtils.ifNull;

import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Instant;
import org.joda.time.Interval;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.BorrowedTransaction;
import wbs.framework.database.Database;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.misc.CachedGetter;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.model.UserRec;

import wbs.smsapps.manualresponder.model.ManualResponderOperatorReport;
import wbs.smsapps.manualresponder.model.ManualResponderRequestSearch;

import wbs.utils.time.TextualInterval;
import wbs.utils.time.TimeFormatter;

@PrototypeComponent ("manualResponderNumThisHourCache")
@Accessors (fluent = true)
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
	TimeFormatter timeFormatter;

	@SingletonDependency
	UserConsoleHelper userHelper;

	@SingletonDependency
	WbsConfig wbsConfig;

	// properties

	@Getter @Setter
	Long userId;

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

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"refresh");

		) {

			BorrowedTransaction transaction =
				database.currentTransaction ();

			UserRec user =
				userHelper.findRequired (
					userId);

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
					userId)

				.processedTime (
					TextualInterval.forInterval (
						timeFormatter.timezone (
							ifNull (
								user.getDefaultTimezone (),
								user.getSlice ().getDefaultTimezone (),
								wbsConfig.defaultTimezone ())),
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

}
