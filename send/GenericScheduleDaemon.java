package wbs.platform.send;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.List;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.experimental.Accessors;

import org.apache.log4j.Logger;
import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;

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

	// hooks

	protected abstract
	GenericSendHelper <Service, Job, Item> helper ();

	protected abstract
	Logger log ();

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
	Duration getSleepDuration () {

		return Duration.standardSeconds (
			5);

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

		log ().debug (
			stringFormat (
				"Looking for scheduled broadcasts to send"));

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"GenericScheduleDaemon.runOnce ()",
				this);

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
			this::runJob);

	}

	void runJob (
			@NonNull Long jobId) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"GenericScheduleDaemon.runJob (jobId)",
				this);

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

			log ().debug (
				stringFormat (
					"Not sending %s because it is not scheduled",
					integerToDecimalString (
						jobId)));

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

			log ().warn (
				stringFormat (
					"Not sending %s because it is scheduled in the future",
					integerToDecimalString (
						jobId)));

			return;

		}

		// move to sending state

		log ().info (
			stringFormat (
				"Sending %s",
				integerToDecimalString (
					jobId)));

		helper ().sendStart (
			service,
			job);

		transaction.commit ();

	}

}
