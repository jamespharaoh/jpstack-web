package wbs.smsapps.autoresponder.daemon;

import static wbs.utils.collection.CollectionUtils.listIndexOfRequired;
import static wbs.utils.collection.CollectionUtils.listItemAtIndexRequired;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

import lombok.Cleanup;
import lombok.NonNull;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;
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

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	DeliveryObjectHelper deliveryHelper;

	@SingletonDependency
	AutoResponderRequestObjectHelper autoResponderRequestHelper;

	@SingletonDependency
	SmsMessageLogic messageLogic;

	@SingletonDependency
	SmsOutboxLogic outboxLogic;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	// details

	@Override
	public
	Collection <String> getDeliveryTypeCodes () {

		return ImmutableList.of (
			"auto_responder");

	}

	// implementation

	@Override
	public
	void handle (
			@NonNull TaskLogger parentTaskLogger,
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

			long deliveryMessageIndex =
				listIndexOfRequired (
					request.getSentMessages (),
					deliveryMessage);

			if (
				request.getSentMessages ().size ()
					> deliveryMessageIndex + 1
			) {

				MessageRec nextMessage =
					listItemAtIndexRequired (
						request.getSentMessages (),
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

			Long deliveryMessageIndex =
				listIndexOfRequired (
					request.getSentMessages (),
					deliveryMessage);

			for (
				long messageIndex = deliveryMessageIndex + 1;
				messageIndex < request.getSentMessages ().size ();
				messageIndex ++
			) {

				MessageRec heldMessage =
					listItemAtIndexRequired (
						request.getSentMessages (),
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
