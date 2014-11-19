package wbs.smsapps.broadcast.logic;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.joda.time.Instant;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectHelper;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.send.GenericSendHelper;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.message.batch.model.BatchObjectHelper;
import wbs.sms.message.batch.model.BatchRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.logic.MessageSender;
import wbs.sms.number.lookup.logic.NumberLookupManager;
import wbs.smsapps.broadcast.model.BroadcastConfigRec;
import wbs.smsapps.broadcast.model.BroadcastNumberObjectHelper;
import wbs.smsapps.broadcast.model.BroadcastNumberRec;
import wbs.smsapps.broadcast.model.BroadcastNumberState;
import wbs.smsapps.broadcast.model.BroadcastObjectHelper;
import wbs.smsapps.broadcast.model.BroadcastRec;
import wbs.smsapps.broadcast.model.BroadcastState;

@SingletonComponent ("broadcastSendHelper")
public
class BroadcastSendHelper
	implements
		GenericSendHelper<
			BroadcastConfigRec,
			BroadcastRec,
			BroadcastNumberRec
		> {

	// dependencies

	@Inject
	BroadcastObjectHelper broadcastHelper;

	@Inject
	BroadcastNumberObjectHelper broadcastNumberHelper;

	@Inject
	BatchObjectHelper batchHelper;

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	NumberLookupManager numberLookupManager;

	@Inject
	ServiceObjectHelper serviceHelper;

	// prototype dependencies

	@Inject
	Provider<MessageSender> messageSenderProvider;

	// details

	public
	String name () {
		return "broadcast";
	}

	public
	String itemNamePlural () {
		return "broadcasts";
	}

	public
	ObjectHelper<BroadcastRec> jobHelper () {
		return broadcastHelper;
	}

	public
	ObjectHelper<BroadcastNumberRec> itemHelper () {
		return broadcastNumberHelper;
	}

	// implementation

	public
	List<BroadcastRec> findSendingJobs () {

		return broadcastHelper.findSending ();

	}

	public
	List<BroadcastRec> findScheduledJobs (
			Instant now) {

		return broadcastHelper.findScheduled (
			now);

	}

	public
	List<BroadcastNumberRec> findItemsLimit (
			BroadcastConfigRec broadcastConfig,
			BroadcastRec broadcast,
			int maxResults) {

		return broadcastNumberHelper.findAcceptedLimit (
			broadcast,
			maxResults);

	}

	public
	BroadcastConfigRec getService (
			BroadcastRec broadcast) {

		return broadcast.getBroadcastConfig ();

	}

	public
	Instant getScheduledTime (
			BroadcastConfigRec service,
			BroadcastRec job) {

		return job.getScheduledTime ();

	}

	public
	boolean isScheduled (
			BroadcastConfigRec broadcastConfig,
			BroadcastRec broadcast) {

		return broadcast.getState ()
			== BroadcastState.scheduled;

	}


	public
	boolean isSending (
			BroadcastConfigRec broadcastConfig,
			BroadcastRec broadcast) {

		return broadcast.getState ()
			== BroadcastState.sending;

	}

	public
	boolean isConfigured (
			BroadcastConfigRec broadcastConfig,
			BroadcastRec broadcast) {

		if (broadcastConfig.getRouter () == null)
			return false;

		return true;

	}

	public
	void sendStart (
			BroadcastConfigRec broadcastConfig,
			BroadcastRec broadcast) {

		broadcast

			.setState (
				BroadcastState.sending);

		broadcastConfig

			.setNumScheduled (
				broadcastConfig.getNumScheduled () - 1)

			.setNumSending (
				broadcastConfig.getNumSending () + 1);

		eventLogic.createEvent (
			"broadcast_send_begun",
			broadcast);

	}

	public
	void sendItem (
			BroadcastConfigRec broadcastConfig,
			BroadcastRec broadcast,
			BroadcastNumberRec broadcastNumber) {

		ServiceRec defaultService =
			serviceHelper.findByCode (
				broadcastConfig,
				"default");

		BatchRec broadcastBatch =
			batchHelper.findByCode (
				broadcast,
				"broadcast");

		// sanity check

		if (broadcastNumber.getState ()
				!= BroadcastNumberState.accepted) {

			throw new RuntimeException ();

		}

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

			return;

		}

		// send the message

		MessageRec message =
			messageSenderProvider.get ()

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

	public
	void sendComplete (
			BroadcastConfigRec broadcastConfig,
			BroadcastRec broadcast) {

		Transaction transaction =
			database.currentTransaction ();

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

}
