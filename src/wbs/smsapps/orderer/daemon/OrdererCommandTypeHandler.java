package wbs.smsapps.orderer.daemon;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.NonNull;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.platform.email.logic.EmailLogic;
import wbs.platform.lock.logic.LockLogic;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.delivery.model.DeliveryDao;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.ReceivedMessage;
import wbs.sms.message.outbox.logic.MessageSender;
import wbs.sms.number.core.model.NumberRec;
import wbs.smsapps.orderer.model.OrdererOrderObjectHelper;
import wbs.smsapps.orderer.model.OrdererOrderRec;
import wbs.smsapps.orderer.model.OrdererRec;

@SingletonComponent ("ordererCommandTypeHandler")
public
class OrdererCommandTypeHandler
	implements CommandHandler {

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	LockLogic coreLogic;

	@Inject
	Database database;

	@Inject
	DeliveryDao deliveryDao;

	@Inject
	EmailLogic emailUtils;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	ObjectManager objectManager;

	@Inject
	OrdererOrderObjectHelper ordererOrderHelper;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	Provider<MessageSender> messageSender;

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"orderer.default"
		};

	}

	@Override
	public
	Status handle (
			int commandId,
			@NonNull ReceivedMessage receivedMessage) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		CommandRec command =
			commandHelper.find (
				commandId);

		OrdererRec orderer =
			(OrdererRec) (Object)
			objectManager.getParent (
				command);

		MessageRec message =
			messageHelper.find (
				receivedMessage.getMessageId ());

		NumberRec number =
			message.getNumber ();

		// set the message service

		message.setService (
			serviceHelper.findByCode (
				orderer,
				"default"));

		// check its not a repeat order

		if (! orderer.getAllowRepeat ()) {

			coreLogic.magicLock (
				orderer,
				number);

			List<OrdererOrderRec> ordererOrders =
				ordererOrderHelper.find (
					orderer,
					number);

			if (! ordererOrders.isEmpty ()) {

				transaction.commit ();

				return Status.processed;

			}

		}

		// create the order record

		OrdererOrderRec order =
			ordererOrderHelper.insert (
				new OrdererOrderRec ()
					.setOrderer (orderer)
					.setNumber (number)
					.setReceivedMessage (message)
					.setText (receivedMessage.getRest ()));

		// send the billed message

		MessageRec billedMessage =
			messageSender.get ()
				.threadId (message.getThreadId ())
				.number (number)
				.messageString (orderer.getBillTemplate ())
				.numFrom (orderer.getBillNumber ())
				.route (orderer.getBillRoute ())
				.serviceLookup (orderer, "default")
				.deliveryTypeCode ("orderer")
				.ref (order.getId ())
				.send ();

		order.setBilledMessage (billedMessage);

		transaction.commit ();

		return Status.processed;

	}

}
