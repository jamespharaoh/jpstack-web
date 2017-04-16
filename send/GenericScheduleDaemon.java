package wbs.platform.send;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.daemon.SleepingDaemonService;

@Accessors (fluent = true)
public abstract
class GenericScheduleDaemon <
	Service extends Record <Service>,
	Job extends Record <Job>,
	Item extends Record <Item>
>
	extends SleepingDaemonService {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	// hooks

	protected abstract
	GenericSendHelper <Service, Job, Item> helper ();

	// details

	@Override
	protected
	String backgroundProcessName () {

		return stringFormat (
			"%s.schedule",
			helper ().parentTypeName ());

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

		taskLogger.debugFormat (
			"Looking for scheduled broadcasts to send");

		try (

			Transaction transaction =
				database.beginReadOnly (
					taskLogger,
					"GenericScheduleDaemon.runOnce ()",
					this);

		) {

			List <Job> jobs =
				helper ().findScheduledJobs (
					transaction.now ());

			List <Long> jobIds =
				new ArrayList<> ();

			for (
				Job job
					: jobs
			) {

				jobIds.add (
					job.getId ());

			}

			transaction.close ();

			jobIds.forEach (
				jobId ->
					runJob (
						taskLogger,
						jobId));

		}

	}

	void runJob (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long jobId) {

		TaskLogger taskLogger =
			logContext.nestTaskLoggerFormat (
				parentTaskLogger,
				"runJob (%s)",
				integerToDecimalString (
					jobId));

		try (

			Transaction transaction =
				database.beginReadWrite (
					taskLogger,
					"GenericScheduleDaemon.runJob (jobId)",
					this);

		) {

			Job job =
				helper ().jobHelper ().findRequired (
					jobId);

			Service service =
				helper ().getService (
					job);

			// check state is still "scheduled"

			if (
				! helper ().jobScheduled (
					service,
					job)
			) {

				taskLogger.debugFormat (
					"Not sending %s because it is not scheduled",
					integerToDecimalString (
						jobId));

				return;

			}

			// check scheduled time is in the future

			Instant scheduledTime =
				helper ().getScheduledTime (
					service,
					job);

			if (
				scheduledTime.isAfter (
					transaction.now ())
			) {

				taskLogger.warningFormat (
					"Not sending %s because it is scheduled in the future",
					integerToDecimalString (
						jobId));

				return;

			}

			// move to sending state

			taskLogger.noticeFormat (
				"Sending %s",
				integerToDecimalString (
					jobId));

			helper ().sendStart (
				taskLogger,
				service,
				job);

			transaction.commit ();

		}

	}

}
