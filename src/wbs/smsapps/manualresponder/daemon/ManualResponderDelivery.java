package wbs.smsapps.manualresponder.daemon;

import static wbs.framework.utils.etc.Misc.indexOfRequired;

import java.util.Arrays;
import java.util.Collection;

import javax.inject.Inject;

import lombok.Cleanup;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.sms.message.core.logic.MessageLogic;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.delivery.daemon.DeliveryHandler;
import wbs.sms.message.delivery.model.DeliveryObjectHelper;
import wbs.sms.message.delivery.model.DeliveryRec;
import wbs.sms.message.outbox.logic.OutboxLogic;
import wbs.smsapps.manualresponder.model.ManualResponderReplyObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderReplyRec;

@PrototypeComponent ("manualResponderDelivery")
public
class ManualResponderDelivery
	implements DeliveryHandler {

	// dependencies

	@Inject
	Database database;

	@Inject
	DeliveryObjectHelper deliveryHelper;

	@Inject
	ManualResponderReplyObjectHelper manualResponderReplyHelper;

	@Inject
	MessageLogic messageLogic;

	@Inject
	OutboxLogic outboxLogic;

	@Inject
	ServiceObjectHelper serviceHelper;

	// details

	@Override
	public
	Collection<String> getDeliveryTypeCodes () {

		return Arrays.<String>asList (
			"manual_responder");

	}

	// implementation

	@Override
	public
	void handle (
			int deliveryId,
			Integer ref) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

System.out.println (
	"MR DELIVERY " + deliveryId);

		DeliveryRec delivery =
			deliveryHelper.find (
				deliveryId);

System.out.println (
	"MESSAGE " + delivery.getMessage ().getId ());

System.out.println (
	"STATUS " + delivery.getMessage ().getStatus ().toString ());

		if (delivery.getNewMessageStatus ().isGoodType ()) {

System.out.println (
	"IS GOOD TYPE");

			MessageRec deliveryMessage =
				delivery.getMessage ();

			ManualResponderReplyRec reply =
				manualResponderReplyHelper.find (
					deliveryMessage.getRef ());

			Integer deliveryMessageIndex =
				indexOfRequired (
					reply.getMessages (),
					deliveryMessage);

System.out.println (
	"DELIVERY INDEX " + deliveryMessageIndex);

System.out.println (
	"MESSAGES SIZE " + reply.getMessages ().size ());

			if (
				reply.getMessages ().size ()
					> deliveryMessageIndex + 1
			) {

				MessageRec nextMessage =
					reply.getMessages ().get (
						deliveryMessageIndex + 1);

System.out.println (
	"NEXT ID " + nextMessage.getId ());

System.out.println (
	"NEXT STATUS " + nextMessage.getStatus ());

				if (nextMessage.getStatus () == MessageStatus.held) {

System.out.println (
	"UNHOLD");

					outboxLogic.unholdMessage (
						nextMessage);

				}

			}

		}

		if (delivery.getNewMessageStatus ().isBadType ()) {

System.out.println (
	"IS BAD TYPE");

			MessageRec deliveryMessage =
				delivery.getMessage ();

			ManualResponderReplyRec reply =
				manualResponderReplyHelper.find (
					deliveryMessage.getRef ());

			Integer deliveryMessageIndex =
				indexOfRequired (
					reply.getMessages (),
					deliveryMessage);

			for (
				int messageIndex = deliveryMessageIndex + 1;
				messageIndex < reply.getMessages ().size ();
				messageIndex ++
			) {

				MessageRec heldMessage =
					reply.getMessages ().get (
						messageIndex);

				if (heldMessage.getStatus () == MessageStatus.held) {

					messageLogic.messageStatus (
						heldMessage,
						MessageStatus.cancelled);

				}

			}

		}

System.out.println (
	"REMOVE");

		deliveryHelper.remove (
			delivery);

		transaction.commit ();

System.out.println (
	"COMMIT");

	}

}
