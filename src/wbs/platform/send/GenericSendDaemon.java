package wbs.platform.send;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.experimental.Accessors;
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
			database.beginReadOnly ();

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

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		Job job =
			helper ().jobHelper ().find (
				jobId);

		Service service =
			helper ().getService (
				job);

		if (
			! helper ().isSending (
				service,
				job)
		) {
			return;
		}

		if (
			! helper ().isConfigured (
				service,
				job)
		) {
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

			helper ().sendComplete (
				service,
				job);

		} else {

			// perform send

			for (
				Item item
					: items
			) {

				helper ().sendItem (
					service,
					job,
					item);

			}

		}

		// commit transaction

		transaction.commit ();

	}

}
