package wbs.smsapps.subscription.daemon;

import java.util.Arrays;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.sms.message.delivery.daemon.DeliveryHandler;
import wbs.sms.message.delivery.model.DeliveryObjectHelper;
import wbs.sms.message.delivery.model.DeliveryRec;
import wbs.sms.message.outbox.logic.MessageSender;
import wbs.smsapps.subscription.model.SubscriptionSendNumberObjectHelper;

@PrototypeComponent ("subscriptionDeliveryNoticeHandler")
public
class SubscriptionDeliveryNoticeHandler
	implements DeliveryHandler {

	// dependencies

	@Inject
	Database database;

	@Inject
	DeliveryObjectHelper deliveryHelper;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	SubscriptionSendNumberObjectHelper subscriptionSendNumberHelper;

	@Inject
	Provider<MessageSender> messageSender;

	// details

	@Override
	public
	Collection<String> getDeliveryTypeCodes () {

		return Arrays.<String>asList (
			"subscription");

	}

	// implementation

	@Override
	public
	void handle (
			int deliveryId,
			Integer ref) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		DeliveryRec delivery =
			deliveryHelper.find (
				deliveryId);

		/*
		MessageRec message =
			delivery.getMessage ();

		SubscriptionSendNumberRec subscriptionSendNumber =
			subscriptionSendNumberHelper.find (
				message.getRef ());

		SubscriptionSendRec subscriptionSend =
			subscriptionSendNumber.getSubscriptionSend ();

		SubscriptionRec subscription =
			subscriptionSend.getSubscription ();

		TemplateVersionRec tv =
			subscriptionSend.getTemplate ().getTemplateVersion ();

		ServiceRec defaultService =
			serviceHelper.findByCode (
				subscription,
				"default");

		// send the free messages if appropriate

		if (
			delivery.getNewMessageStatus ().isGoodType ()
			&& subscriptionSendNumber.getState ()
				!= SubscriptionSendState.) {

			for (TemplatePartRec templatePart
					: tv.getTemplateParts ()) {

				messageSender.get ()
					.threadId (subscriptionSendNumber.getThreadId ())
					.number (subscriptionSendNumber.getNumber ())
					.messageString (templatePart.getMessage ())
					.numFrom (subscription.getFreeNumber ())
					.route (subscription.getFreeRoute ())
					.service (defaultService)
					.batch (subscriptionSend.getBatch ())
					.send ();

			}

			subscriptionSendNumber.setSent (true);

		}
		*/

		// delete the dnq

		deliveryHelper.remove (
			delivery);

		transaction.commit ();

	}

}
