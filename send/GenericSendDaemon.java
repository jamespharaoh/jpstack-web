package wbs.platform.send;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.experimental.Accessors;

import org.apache.log4j.Logger;

import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.Record;
import wbs.platform.daemon.SleepingDaemonService;

@Accessors (fluent = true)
public abstract
class GenericSendDaemon<
	Service extends Record<Service>,
	Job extends Record<Job>,
	Item extends Record<Item>
>
	extends SleepingDaemonService {

	// dependencies

	@Inject
	Database database;

	// hooks

	protected abstract
	Logger log ();

	protected abstract
	GenericSendHelper<Service,Job,Item> helper ();

	// details

	@Override
	protected
	String getThreadName () {

		return stringFormat (
			"%sSend",
			capitalise (
				helper ().name ()));

	}

	@Override
	protected
	int getDelayMs () {
		return 5000;
	}

	@Override
	protected
	String generalErrorSource () {

		return stringFormat (
			"%s send daemon",
			helper ().name ());

	}

	@Override
	protected
	String generalErrorSummary () {

		return stringFormat (
			"error sending %s in background",
			helper ().itemNamePlural ());

	}

	// implementation

	@Override
	protected
	void runOnce () {

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				this);

		log ().debug (
			stringFormat (
				"Looking for jobs in sending state"));

		List<Job> jobs =
			helper ().findSendingJobs ();

		List<Integer> jobIds =
			new ArrayList<Integer> ();

		for (
			Job job
				: jobs
		) {

			jobIds.add (
				job.getId ());

		}

		transaction.close ();

		for (
			Integer jobId
				: jobIds
		) {

			runJob (
				jobId);

		}

	}

	void runJob (
			int jobId) {

		log ().debug (
			stringFormat (
				"Performing send for %s",
				jobId));

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		Job job =
			helper ().jobHelper ().findOrNull (
				jobId);

		Service service =
			helper ().getService (
				job);

		if (
			! helper ().jobSending (
				service,
				job)
		) {

			log ().debug (
				stringFormat (
					"Not sending job %s",
					jobId));

			return;

		}

		if (
			! helper ().jobConfigured (
				service,
				job)
		) {

			log ().warn (
				stringFormat (
					"Not configured job %s",
					jobId));

			return;

		}

		List<Item> items =
			helper ().findItemsLimit (
				service,
				job,
				100);

		if (
			items.isEmpty ()
		) {

			// handle completion

			log ().debug (
				stringFormat (
					"Triggering completion for job %s",
					jobId));

			helper ().sendComplete (
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
						service,
						job,
						item);

				if (itemVerified) {

					log ().debug (
						stringFormat (
							"Sending item %s",
							item.getId ()));

					// send item

					helper ().sendItem (
						service,
						job,
						item);

				} else {

					log ().debug (
						stringFormat (
							"Rejecting item %s",
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

		log ().debug (
			stringFormat (
				"Finished send for job %s",
				jobId));

	}

}
