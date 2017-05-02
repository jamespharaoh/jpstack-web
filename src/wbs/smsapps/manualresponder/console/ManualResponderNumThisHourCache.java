package wbs.smsapps.manualresponder.console;

import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.etc.NullUtils.ifNull;

import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.Interval;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.DatabaseCachedGetter;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.model.UserRec;

import wbs.smsapps.manualresponder.model.ManualResponderOperatorReport;
import wbs.smsapps.manualresponder.model.ManualResponderRequestSearch;

import wbs.utils.cache.CachedGetter;
import wbs.utils.time.TextualInterval;
import wbs.utils.time.TimeFormatter;

@PrototypeComponent ("manualResponderNumThisHourCache")
@Accessors (fluent = true)
public
class ManualResponderNumThisHourCache
	implements CachedGetter <Transaction, Long> {

	// singleton dependencies

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

	// state

	private
	CachedGetter <Transaction, Long> delegate;

	// life cycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			delegate =
				new DatabaseCachedGetter<> (
					logContext,
					this::refresh,
					Duration.standardSeconds (
						5l));

		}

	}

	// public implementation

	@Override
	public
	Long get (
			@NonNull Transaction context) {

		return delegate.get (
			context);

	}

	// private implementation

	private
	Long refresh (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"refresh");

		) {

			UserRec user =
				userHelper.findRequired (
					transaction,
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
					transaction,
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
