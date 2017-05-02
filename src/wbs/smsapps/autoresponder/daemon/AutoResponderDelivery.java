package wbs.smsapps.autoresponder.daemon;

import static wbs.utils.collection.CollectionUtils.listIndexOfRequired;
import static wbs.utils.collection.CollectionUtils.listItemAtIndexRequired;
import static wbs.utils.string.StringUtils.keyEqualsDecimalInteger;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

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

import wbs.smsapps.autoresponder.model.AutoResponderRequestObjectHelper;
import wbs.smsapps.autoresponder.model.AutoResponderRequestRec;

@PrototypeComponent ("autoResponderDelivery")
public
class AutoResponderDelivery
	implements DeliveryHandler {

	// singleton dependencies

	@SingletonDependency
	AutoResponderRequestObjectHelper autoResponderRequestHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	DeliveryObjectHelper deliveryHelper;

	@ClassSingletonDependency
	LogContext logContext;

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

		try (

			OwnedTransaction transaction =
				database.beginReadWriteFormat (
					logContext,
					parentTaskLogger,
					"handle (%s, %s)",
					keyEqualsDecimalInteger (
						"deliveryId",
						deliveryId),
					keyEqualsDecimalInteger (
						"ref",
						ref));

		) {

			DeliveryRec delivery =
				deliveryHelper.findRequired (
					transaction,
					deliveryId);

			if (delivery.getNewMessageStatus ().isGoodType ()) {

				MessageRec deliveryMessage =
					delivery.getMessage ();

				AutoResponderRequestRec request =
					autoResponderRequestHelper.findRequired (
						transaction,
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
							transaction,
							nextMessage);

					}

				}

			}

			if (delivery.getNewMessageStatus ().isBadType ()) {

				MessageRec deliveryMessage =
					delivery.getMessage ();

				AutoResponderRequestRec request =
					autoResponderRequestHelper.findRequired (
						transaction,
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
