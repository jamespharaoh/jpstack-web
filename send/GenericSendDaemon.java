package wbs.platform.send;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.experimental.Accessors;

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
class GenericSendDaemon <
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
			"%s.send",
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

		try (

			Transaction transaction =
				database.beginReadOnly (
					"GenericSendDaemon.runOnce ()",
					this);

		) {

			taskLogger.debugFormat (
				"Looking for jobs in sending state");

			List <Job> jobs =
				helper ().findSendingJobs ();

			List <Long> jobIds =
				jobs.stream ()

				.map (
					Job::getId)

				.collect (
					Collectors.toList ());

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

		taskLogger.debugFormat (
			"Performing send for %s",
			integerToDecimalString (
				jobId));

		try (

			Transaction transaction =
				database.beginReadWrite (
					"GenericSendDaemon.runJob (jobId)",
					this);

		) {

			Job job =
				helper ().jobHelper ().findRequired (
					jobId);

			Service service =
				helper ().getService (
					job);

			if (
				! helper ().jobSending (
					service,
					job)
			) {

				taskLogger.debugFormat (
					"Not sending job %s",
					integerToDecimalString (
						jobId));

				return;

			}

			if (
				! helper ().jobConfigured (
					service,
					job)
			) {

				taskLogger.warningFormat (
					"Not configured job %s",
					integerToDecimalString (
						jobId));

				return;

			}

			List <Item> items =
				helper ().findItemsLimit (
					service,
					job,
					100);

			if (
				items.isEmpty ()
			) {

				// handle completion

				taskLogger.debugFormat (
					"Triggering completion for job %s",
					integerToDecimalString (
						jobId));

				helper ().sendComplete (
					taskLogger,
					service,
					job);

			} else {

				// perform send

				for (
					Item item
						: items
				) {

					// verify the item

					boolean itemVerified =
						helper ().verifyItem (
							taskLogger,
							service,
							job,
							item);

					if (itemVerified) {

						taskLogger.debugFormat (
							"Sending item %s",
							integerToDecimalString (
								item.getId ()));

						// send item

						helper ().sendItem (
							taskLogger,
							service,
							job,
							item);

					} else {

						taskLogger.debugFormat (
							"Rejecting item %s",
							integerToDecimalString (
								item.getId ()));

						// reject it

						helper ().rejectItem (
							service,
							job,
							item);

					}

				}

			}

			// commit transaction

			transaction.commit ();

			taskLogger.debugFormat (
				"Finished send for job %s",
				integerToDecimalString (
					jobId));

		}

	}

}
