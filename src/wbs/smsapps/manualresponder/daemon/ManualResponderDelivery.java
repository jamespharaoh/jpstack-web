package wbs.smsapps.manualresponder.daemon;

import java.util.Arrays;
import java.util.Collection;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.service.model.ServiceObjectHelper;
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

		DeliveryRec delivery =
			deliveryHelper.find (
				deliveryId);

		if (delivery.getNewMessageStatus ().isGoodType ()) {

			MessageRec deliveryMessage =
				delivery.getMessage ();

			ManualResponderReplyRec reply =
				manualResponderReplyHelper.find (
					deliveryMessage.getRef ());

			Integer deliveryMessageIndex =
				reply.getMessages ().indexOf (
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

		deliveryHelper.remove (
			delivery);

		transaction.commit ();

	}

}
