package wbs.platform.send;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import org.joda.time.Instant;

import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.Record;
import wbs.platform.daemon.SleepingDaemonService;

@Accessors (fluent = true)
@Log4j
public abstract
class GenericScheduleDaemon<
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
			"%sSchedule",
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
			"%s schedule daemon",
			helper ().name ());

	}

	@Override
	protected
	String generalErrorSummary () {

		return stringFormat (
			"error checking for schedule %s to begin sending",
			helper ().itemNamePlural ());

	}

	@Override
	protected
	void runOnce () {

		log.debug (
			stringFormat (
				"Looking for scheduled broadcasts to send"));

		@Cleanup
		Transaction transaction =
			database.beginReadOnly ();

		List<Job> jobs =
			helper ().findScheduledJobs (
				transaction.now ());

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

		for (Integer jobId
				: jobIds) {

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

		// check state is still "scheduled"

		if (
			! helper ().jobScheduled (
				service,
				job)
		) {

			log.debug (
				stringFormat (
					"Not sending %s because it is not scheduled",
					job));

			return;

		}

		// check scheduled time is in the future

		Instant scheduledTime =
			helper ().getScheduledTime (
				service,
				job);

		if (
			scheduledTime.isBefore (
				transaction.now ())
		) {

			log.debug (
				stringFormat (
					"Not sending %s because it is scheduled in the future",
					job));

			return;

		}

		// move to sending state

		log.info (
			stringFormat (
				"Sending %s",
				job));

		helper ().sendStart (
			service,
			job);

		transaction.commit ();

	}

}
