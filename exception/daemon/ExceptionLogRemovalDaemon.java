package wbs.platform.exception.daemon;

import static wbs.framework.utils.etc.Misc.isEmpty;
import static wbs.framework.utils.etc.Misc.isEmptyString;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;

import org.joda.time.Instant;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.platform.daemon.SleepingDaemonService;
import wbs.platform.exception.model.ExceptionLogObjectHelper;
import wbs.platform.exception.model.ExceptionLogRec;

@Log4j
@SingletonComponent ("exceptionLogRemovalDaemon")
public
class ExceptionLogRemovalDaemon
	extends SleepingDaemonService {

	// dependencies

	@Inject
	Database database;

	@Inject
	ExceptionLogObjectHelper exceptionLogHelper;

	@Inject
	ExceptionLogger exceptionLogger;

	// details

	@Override
	protected
	String getThreadName () {
		return "ExceptionLogRemoval";
	}

	@Override
	protected
	int getDelayMs () {
		return 60 * 1000;
	}

	@Override
	protected
	String generalErrorSource () {
		return "exception log removal daemon";
	}

	@Override
	protected
	String generalErrorSummary () {
		return "error checking for old exception logs to remove";
	}

	// implementation

	@Override
	protected
	void runOnce () {

		for (;;) {

			// get a list of old exception logs

			@Cleanup
			Transaction transaction =
				database.beginReadWrite (
					"ExceptionLogRemovalDaemon.runOnce ()",
					this);

			Instant cutoffTime =
				transaction.now ()
					.toDateTime ()
					.minusWeeks (1)
					.toInstant ();

			List<ExceptionLogRec> oldExceptionLogs =
				exceptionLogHelper.findOldLimit (
					cutoffTime,
					1000);

			if (
				isEmpty (
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

			log.info (
				stringFormat (
					"Removed %s old exception logs",
					oldExceptionLogs.size ()));

		}

	}

}
