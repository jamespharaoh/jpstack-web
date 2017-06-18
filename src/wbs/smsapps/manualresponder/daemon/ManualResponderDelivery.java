package wbs.smsapps.manualresponder.daemon;

import static wbs.utils.collection.CollectionUtils.listIndexOfRequired;
import static wbs.utils.collection.CollectionUtils.listItemAtIndexRequired;

import java.util.Arrays;
import java.util.Collection;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.service.model.ServiceObjectHelper;

import wbs.sms.message.core.logic.SmsMessageLogic;
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

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	DeliveryObjectHelper deliveryHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ManualResponderReplyObjectHelper manualResponderReplyHelper;

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

		return Arrays.<String>asList (
			"manual_responder");

	}

	// implementation

	@Override
	public
	void handle (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long deliveryId,
			@NonNull Long ref) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"handle");

		) {

			DeliveryRec delivery =
				deliveryHelper.findRequired (
					transaction,
					deliveryId);

			if (delivery.getNewMessageStatus ().isGoodType ()) {

				MessageRec deliveryMessage =
					delivery.getMessage ();

				ManualResponderReplyRec reply =
					manualResponderReplyHelper.findRequired (
						transaction,
						deliveryMessage.getRef ());

				Long deliveryMessageIndex =
					listIndexOfRequired (
						reply.getMessages (),
						deliveryMessage);

				if (
					reply.getMessages ().size ()
						> deliveryMessageIndex + 1
				) {

					MessageRec nextMessage =
						listItemAtIndexRequired (
							reply.getMessages (),
							deliveryMessageIndex + 1);

					if (nextMessage.getStatus () == MessageStatus.held) {

						outboxLogic.unholdMessage (
							transaction,
							nextMessage);

					}

				}

			}

			if (delivery.getNewMessageStatus ().isBadType ()) {

				MessageRec deliveryMessage =
					delivery.getMessage ();

				ManualResponderReplyRec reply =
					manualResponderReplyHelper.findRequired (
						transaction,
						deliveryMessage.getRef ());

				Long deliveryMessageIndex =
					listIndexOfRequired (
						reply.getMessages (),
						deliveryMessage);

				for (
					long messageIndex = deliveryMessageIndex + 1;
					messageIndex < reply.getMessages ().size ();
					messageIndex ++
				) {

					MessageRec heldMessage =
						listItemAtIndexRequired (
							reply.getMessages (),
							messageIndex);

					if (heldMessage.getStatus () == MessageStatus.held) {

						messageLogic.messageStatus (
							transaction,
							heldMessage,
							MessageStatus.cancelled);

					}

				}

			}

			deliveryHelper.remove (
				transaction,
				delivery);

			transaction.commit ();

		}

	}

}
