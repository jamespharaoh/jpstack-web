package wbs.smsapps.broadcast.logic;

import java.util.List;

import javax.inject.Provider;

import org.joda.time.Instant;

import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
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
import wbs.sms.message.outbox.logic.SmsMessageSender;
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
		GenericSendHelper <
			BroadcastConfigRec,
			BroadcastRec,
			BroadcastNumberRec
		> {

	// singleton dependencies

	@SingletonDependency
	BroadcastObjectHelper broadcastHelper;

	@SingletonDependency
	BroadcastNumberObjectHelper broadcastNumberHelper;

	@SingletonDependency
	BatchObjectHelper batchHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@SingletonDependency
	NumberLookupManager numberLookupManager;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <SmsMessageSender> smsMessageSenderProvider;

	// details

	@Override
	public
	String name () {
		return "broadcast";
	}

	@Override
	public
	String itemNamePlural () {
		return "broadcasts";
	}

	@Override
	public
	ObjectHelper<BroadcastRec> jobHelper () {
		return broadcastHelper;
	}

	@Override
	public
	ObjectHelper<BroadcastNumberRec> itemHelper () {
		return broadcastNumberHelper;
	}

	// implementation

	@Override
	public
	List<BroadcastRec> findSendingJobs () {
		return broadcastHelper.findSending ();
	}

	@Override
	public
	List<BroadcastRec> findScheduledJobs (
			Instant now) {

		return broadcastHelper.findScheduled (
			now);

	}

	@Override
	public
	List<BroadcastNumberRec> findItemsLimit (
			BroadcastConfigRec broadcastConfig,
			BroadcastRec broadcast,
			int maxResults) {

		return broadcastNumberHelper.findAcceptedLimit (
			broadcast,
			maxResults);

	}

	@Override
	public
	BroadcastConfigRec getService (
			BroadcastRec broadcast) {

		return broadcast.getBroadcastConfig ();

	}

	@Override
	public
	Instant getScheduledTime (
			BroadcastConfigRec service,
			BroadcastRec job) {

		return job.getScheduledTime ();

	}

	@Override
	public
	boolean jobScheduled (
			BroadcastConfigRec broadcastConfig,
			BroadcastRec broadcast) {

		return broadcast.getState ()
			== BroadcastState.scheduled;

	}


	@Override
	public
	boolean jobSending (
			BroadcastConfigRec broadcastConfig,
			BroadcastRec broadcast) {

		return broadcast.getState ()
			== BroadcastState.sending;

	}

	@Override
	public
	boolean jobConfigured (
			BroadcastConfigRec broadcastConfig,
			BroadcastRec broadcast) {

		if (broadcastConfig.getRouter () == null)
			return false;

		return true;

	}

	@Override
	public
	void sendStart (
			BroadcastConfigRec broadcastConfig,
			BroadcastRec broadcast) {

		// sanity check

		if (
			broadcast.getState ()
				!= BroadcastState.scheduled
		) {
			throw new IllegalStateException ();
		}

		// update broadcast

		broadcast

			.setState (
				BroadcastState.sending);

		broadcastConfig

			.setNumScheduled (
				broadcastConfig.getNumScheduled () - 1)

			.setNumSending (
				broadcastConfig.getNumSending () + 1);

		// create event

		eventLogic.createEvent (
			"broadcast_send_begun",
			broadcast);

	}

	@Override
	public
	boolean verifyItem (
			BroadcastConfigRec broadcastConfig,
			BroadcastRec broadcast,
			BroadcastNumberRec broadcastNumber) {

		// check if block list configured

		if (broadcastConfig.getBlockNumberLookup () == null)
			return true;

		// reject numbers on block list

		if (
			numberLookupManager.lookupNumber (
				broadcastConfig.getBlockNumberLookup (),
				broadcastNumber.getNumber ())
		) {
			return false;
		}

		// accept otherwise

		return true;

	}

	@Override
	public
	void rejectItem (
			BroadcastConfigRec broadcastConfig,
			BroadcastRec broadcast,
			BroadcastNumberRec broadcastNumber) {

		// sanity check

		if (
			broadcastNumber.getState ()
				!= BroadcastNumberState.accepted
		) {
			throw new IllegalStateException ();
		}

		// update number

		broadcastNumber

			.setState (
				BroadcastNumberState.rejected);

		broadcast

			.setNumAccepted (
				broadcast.getNumAccepted () - 1)

			.setNumRejected (
				broadcast.getNumRejected () + 1)

			.setNumTotal (
				broadcast.getNumTotal () - 1);

	}

	@Override
	public
	void sendItem (
			BroadcastConfigRec broadcastConfig,
			BroadcastRec broadcast,
			BroadcastNumberRec broadcastNumber) {

		// sanity check

		if (
			broadcastNumber.getState ()
				!= BroadcastNumberState.accepted
		) {
			throw new IllegalStateException ();
		}

		// send the message

		ServiceRec defaultService =
			serviceHelper.findByCodeRequired (
				broadcastConfig,
				"default");

		BatchRec broadcastBatch =
			batchHelper.findByCodeRequired (
				broadcast,
				"broadcast");

		MessageRec message =
			smsMessageSenderProvider.get ()

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

	@Override
	public
	void sendComplete (
			BroadcastConfigRec broadcastConfig,
			BroadcastRec broadcast) {

		Transaction transaction =
			database.currentTransaction ();

		// sanity check

		if (broadcast.getNumAccepted () != 0) {
			throw new IllegalStateException ();
		}

		// update broadcast

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

		// create event

		eventLogic.createEvent (
			"broadcast_send_completed",
			broadcast);

	}

}
