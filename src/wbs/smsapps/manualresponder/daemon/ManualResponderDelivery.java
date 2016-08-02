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
import wbs.sms.message.outbox.logic.SmsOutboxLogic;
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
	SmsOutboxLogic outboxLogic;

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
			Long ref) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ManualResopnderDelivery.handle (deliveryId, ref)",
				this);

		DeliveryRec delivery =
			deliveryHelper.findRequired (
				deliveryId);

		if (delivery.getNewMessageStatus ().isGoodType ()) {

			MessageRec deliveryMessage =
				delivery.getMessage ();

			ManualResponderReplyRec reply =
				manualResponderReplyHelper.findRequired (
					deliveryMessage.getRef ());

			Integer deliveryMessageIndex =
				indexOfRequired (
					reply.getMessages (),
					deliveryMessage);

			if (
				reply.getMessages ().size ()
					> deliveryMessageIndex + 1
			) {

				MessageRec nextMessage =
					reply.getMessages ().get (
						deliveryMessageIndex + 1);

				if (nextMessage.getStatus () == MessageStatus.held) {

					outboxLogic.unholdMessage (
						nextMessage);

				}

			}

		}

		if (delivery.getNewMessageStatus ().isBadType ()) {

			MessageRec deliveryMessage =
				delivery.getMessage ();

			ManualResponderReplyRec reply =
				manualResponderReplyHelper.findRequired (
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

		deliveryHelper.remove (
			delivery);

		transaction.commit ();

	}

}
