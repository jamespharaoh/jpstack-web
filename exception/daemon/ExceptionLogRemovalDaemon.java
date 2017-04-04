package wbs.platform.exception.daemon;

import static wbs.utils.collection.CollectionUtils.collectionIsEmpty;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;

import java.util.List;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.daemon.SleepingDaemonService;
import wbs.platform.exception.model.ExceptionLogObjectHelper;
import wbs.platform.exception.model.ExceptionLogRec;

@SingletonComponent ("exceptionLogRemovalDaemon")
public
class ExceptionLogRemovalDaemon
	extends SleepingDaemonService {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionLogObjectHelper exceptionLogHelper;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@ClassSingletonDependency
	LogContext logContext;

	// details

	@Override
	protected
	String backgroundProcessName () {
		return "exception-log.removal";
	}

	// implementation

	@Override
	protected
	void runOnce (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"runOnce ()");

		try (

			Transaction transaction =
				database.beginReadWrite (
					"ExceptionLogRemovalDaemon.runOnce ()",
					this);

		) {

			// get a list of old exception logs

			Instant cutoffTime =
				transaction.now ()
					.toDateTime ()
					.minusWeeks (1)
					.toInstant ();

			List <ExceptionLogRec> oldExceptionLogs =
				exceptionLogHelper.findOldLimit (
					cutoffTime,
					1000l);

			if (
				collectionIsEmpty (
					oldExceptionLogs)
			) {
				return;
			}

			// then do each one

			for (
				ExceptionLogRec exceptionLog
					: oldExceptionLogs
			) {

				exceptionLogHelper.remove (
					exceptionLog);

			}

			transaction.commit ();

			taskLogger.noticeFormat (
				"Removed %s old exception logs",
				integerToDecimalString (
					oldExceptionLogs.size ()));

		}

	}

}
