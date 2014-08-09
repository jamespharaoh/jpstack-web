package wbs.smsapps.broadcast.daemon;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.daemon.SleepingDaemonService;
import wbs.platform.event.logic.EventLogic;
import wbs.smsapps.broadcast.model.BroadcastConfigRec;
import wbs.smsapps.broadcast.model.BroadcastObjectHelper;
import wbs.smsapps.broadcast.model.BroadcastRec;
import wbs.smsapps.broadcast.model.BroadcastState;

@Log4j
@SingletonComponent ("broadcastScheduleDaemon")
public
class BroadcastScheduleDaemon
	extends SleepingDaemonService {

	@Inject
	BroadcastObjectHelper broadcastHelper;

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Override
	protected
	int getDelayMs () {
		return 5000;
	}

	@Override
	protected
	String generalErrorSource () {
		return "broadcast schedule daemon";
	}

	@Override
	protected
	String generalErrorSummary () {
		return "error checking for schedule broadcasts to begin sending";
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

		List<BroadcastRec> broadcasts =
			broadcastHelper.findScheduled (
				transaction.now ());

		List<Integer> broadcastIds =
			new ArrayList<Integer> ();

		for (BroadcastRec broadcast : broadcasts)
			broadcastIds.add (broadcast.getId ());

		transaction.close ();

		for (Integer broadcastId
				: broadcastIds) {

			runBroadcast (broadcastId);

		}

	}

	void runBroadcast (
			int broadcastId) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		BroadcastRec broadcast =
			broadcastHelper.find (broadcastId);

		BroadcastConfigRec broadcastConfig =
			broadcast.getBroadcastConfig ();

		if (broadcast.getState () != BroadcastState.scheduled) {

			log.debug (
				stringFormat (
					"Not sending %s because it is not scheduled",
					broadcast));

			return;

		}

		if (! broadcast.getScheduledTime ().isBefore (
				transaction.now ())) {

			log.debug (
				stringFormat (
					"Not sending %s because it is scheduled in the future",
					broadcast));

			return;

		}

		log.info (
			stringFormat (
				"Sending %s",
				broadcast));

		broadcast
			.setState (BroadcastState.sending);

		broadcastConfig
			.setNumScheduled (
				broadcastConfig.getNumScheduled () - 1)
			.setNumSending (
				broadcastConfig.getNumSending () + 1);

		eventLogic.createEvent (
			"broadcast_send_begun",
			broadcast);

		transaction.commit ();

	}

}
