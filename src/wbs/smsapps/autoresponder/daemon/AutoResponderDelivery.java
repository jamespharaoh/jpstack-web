package wbs.smsapps.autoresponder.daemon;

import static wbs.framework.utils.etc.Misc.indexOfRequired;

import java.util.Arrays;
import java.util.Collection;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.NonNull;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.sms.message.core.logic.SmsMessageLogic;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.delivery.daemon.DeliveryHandler;
import wbs.sms.message.delivery.model.DeliveryObjectHelper;
import wbs.sms.message.delivery.model.DeliveryRec;
import wbs.sms.message.outbox.logic.SmsOutboxLogic;
import wbs.smsapps.autoresponder.model.AutoResponderRequestObjectHelper;
import wbs.smsapps.autoresponder.model.AutoResponderRequestRec;

@PrototypeComponent ("autoResponderDelivery")
public
class AutoResponderDelivery
	implements DeliveryHandler {

	// dependencies

	@Inject
	Database database;

	@Inject
	DeliveryObjectHelper deliveryHelper;

	@Inject
	AutoResponderRequestObjectHelper autoResponderRequestHelper;

	@Inject
	SmsMessageLogic messageLogic;

	@Inject
	SmsOutboxLogic outboxLogic;

	@Inject
	ServiceObjectHelper serviceHelper;

	// details

	@Override
	public
	Collection<String> getDeliveryTypeCodes () {

		return Arrays.<String>asList (
			"auto_responder");

	}

	// implementation

	@Override
	public
	void handle (
			@NonNull Long deliveryId,
			@NonNull Long ref) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"AutoResponderDelivery.handle (deliveryId, ref)",
				this);

		DeliveryRec delivery =
			deliveryHelper.findRequired (
				deliveryId);

		if (delivery.getNewMessageStatus ().isGoodType ()) {

			MessageRec deliveryMessage =
				delivery.getMessage ();

			AutoResponderRequestRec request =
				autoResponderRequestHelper.findRequired (
					deliveryMessage.getRef ());

			int deliveryMessageIndex =
				indexOfRequired (
					request.getSentMessages (),
					deliveryMessage);

			if (
				request.getSentMessages ().size ()
					> deliveryMessageIndex + 1
			) {

				MessageRec nextMessage =
					request.getSentMessages ().get (
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

			AutoResponderRequestRec request =
				autoResponderRequestHelper.findRequired (
					deliveryMessage.getRef ());

			Integer deliveryMessageIndex =
				indexOfRequired (
					request.getSentMessages (),
					deliveryMessage);

			for (
				int messageIndex = deliveryMessageIndex + 1;
				messageIndex < request.getSentMessages ().size ();
				messageIndex ++
			) {

				MessageRec heldMessage =
					request.getSentMessages ().get (
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
