package wbs.smsapps.broadcast.daemon;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.daemon.SleepingDaemonService;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.message.batch.model.BatchObjectHelper;
import wbs.sms.message.batch.model.BatchRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.logic.MessageSender;
import wbs.sms.number.lookup.logic.NumberLookupManager;
import wbs.sms.route.router.logic.RouterLogic;
import wbs.smsapps.broadcast.model.BroadcastConfigRec;
import wbs.smsapps.broadcast.model.BroadcastNumberObjectHelper;
import wbs.smsapps.broadcast.model.BroadcastNumberRec;
import wbs.smsapps.broadcast.model.BroadcastNumberState;
import wbs.smsapps.broadcast.model.BroadcastObjectHelper;
import wbs.smsapps.broadcast.model.BroadcastRec;
import wbs.smsapps.broadcast.model.BroadcastState;

@SingletonComponent ("broadcastSendDaemon")
public
class BroadcastSendDaemon
	extends SleepingDaemonService {

	// dependencies

	@Inject
	BatchObjectHelper batchHelper;

	@Inject
	BroadcastObjectHelper broadcastHelper;

	@Inject
	BroadcastNumberObjectHelper broadcastNumberHelper;

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	NumberLookupManager numberLookupManager;

	@Inject
	RouterLogic routerLogic;

	@Inject
	ServiceObjectHelper serviceHelper;

	// prototype dependencies

	@Inject
	Provider<MessageSender> messageSender;

	// details

	@Override
	protected
	int getDelayMs () {
		return 5000;
	}

	@Override
	protected
	String generalErrorSource () {
		return "broadcast send daemon";
	}

	@Override
	protected
	String generalErrorSummary () {
		return "error sending broadcasts in background";
	}

	// implementation

	@Override
	protected
	void runOnce () {

		@Cleanup
		Transaction transaction =
			database.beginReadOnly ();

		List<BroadcastRec> broadcasts =
			broadcastHelper.findSending ();

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

		if (broadcast.getState () != BroadcastState.sending)
			return;

		BroadcastConfigRec broadcastConfig =
			broadcast.getBroadcastConfig ();

		if (broadcastConfig.getRouter () == null)
			return;

		List<BroadcastNumberRec> broadcastNumbers =
			broadcastNumberHelper.findAcceptedLimit (
				broadcast,
				100);

		ServiceRec defaultService =
			serviceHelper.findByCode (
				broadcastConfig,
				"default");

		BatchRec broadcastBatch =
			batchHelper.findByCode (
				broadcast,
				"broadcast");

		for (BroadcastNumberRec broadcastNumber
				: broadcastNumbers) {

			if (broadcastNumber.getState ()
					!= BroadcastNumberState.accepted)
				throw new RuntimeException ();

			// check the block list again

			boolean reject =
				broadcastConfig.getBlockNumberLookup () != null
					? numberLookupManager.lookupNumber (
						broadcastConfig.getBlockNumberLookup (),
						broadcastNumber.getNumber ())
					: false;

			if (reject) {

				// mark the number as rejected

				broadcastNumber

					.setState (
						BroadcastNumberState.rejected);

				broadcast

					.setNumAccepted (
						broadcast.getNumAccepted () - 1)

					.setNumRejected (
						broadcast.getNumRejected () + 1);

			} else {

				// send the message

				MessageRec message =
					messageSender.get ()

					.batch (
						broadcastBatch)

					.messageString (
						broadcast.getMessageText ())

					.number (
						broadcastNumber.getNumber ())

					.numFrom (
						broadcast.getMessageOriginator ())

					.routerResolve (
						broadcastConfig.getRouter ())

					.service (
						defaultService)

					.user (
						broadcast.getSentUser ())

					.send ();

				// mark the number as sent

				broadcastNumber

					.setState (
						BroadcastNumberState.sent)

					.setMessage (
						message);

				broadcast

					.setNumAccepted (
						broadcast.getNumAccepted () - 1)

					.setNumSent (
						broadcast.getNumSent () + 1);

			}

		}

		// mark the broadcast as sent when appropriate

		if (broadcast.getNumAccepted () == 0) {

			broadcast

				.setState (
					BroadcastState.sent)

				.setSentTime (
					transaction.now ());

			broadcastConfig

				.setNumSending (
					broadcastConfig.getNumSending () - 1)

				.setNumSent (
					broadcastConfig.getNumSent () + 1);

			eventLogic.createEvent (
				"broadcast_send_completed",
				broadcast);

		}

		transaction.commit ();

	}

}
