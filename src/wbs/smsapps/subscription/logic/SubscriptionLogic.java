package wbs.smsapps.subscription.logic;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.object.ObjectManager;
import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.sms.message.batch.model.BatchRec;
import wbs.sms.message.batch.model.BatchSubjectObjectHelper;
import wbs.sms.message.batch.model.BatchSubjectRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.logic.MessageSender;
import wbs.sms.template.model.TemplatePartRec;
import wbs.sms.template.model.TemplateVersionRec;
import wbs.smsapps.subscription.model.SubscriptionAffiliateRec;
import wbs.smsapps.subscription.model.SubscriptionObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionRec;
import wbs.smsapps.subscription.model.SubscriptionSendNumberObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionSendNumberRec;
import wbs.smsapps.subscription.model.SubscriptionSendRec;
import wbs.smsapps.subscription.model.SubscriptionSubObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionSubRec;

@SingletonComponent ("subscriptionLogic")
public
class SubscriptionLogic {

	@Inject
	AffiliateObjectHelper affiliateHelper;

	@Inject
	BatchSubjectObjectHelper batchSubjectHelper;

	@Inject
	ObjectManager objectManager;

	@Inject
	SubscriptionObjectHelper subscriptionHelper;

	@Inject
	SubscriptionSendNumberObjectHelper subscriptionSendNumberHelper;

	@Inject
	SubscriptionSubObjectHelper subscriptionSubHelper;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	TextObjectHelper textHelper;

	@Inject
	Provider<MessageSender> messageSender;

	public
	void subscriptionSend (
			SubscriptionSendRec send) {

		SubscriptionRec subscription =
			send.getSubscription ();

		TemplateVersionRec templateVersion =
			send.getTemplate ().getTemplateVersion ();

		// create a batch

		BatchSubjectRec batchSubject =
			batchSubjectHelper.findByCode (
				subscription,
				"end");

		BatchRec batch =
			objectManager.insert (
				new BatchRec ()
					.setSubject (batchSubject));

		send.setBatch (batch);

		// send the message

		TextRec billedMessageText =
			templateVersion.getBilledEnabled ()
				? textHelper.findOrCreate (
					templateVersion.getBilledMessage ())
				: null;

		List<SubscriptionSubRec> subs =
			subscriptionSubHelper.findActive (
				subscription);

		ServiceRec defaultService =
			serviceHelper.findByCode (
				subscription,
				"default");

		for (SubscriptionSubRec sub : subs) {

			SubscriptionSendNumberRec subscriptionSendNumber =
				new SubscriptionSendNumberRec ();

			subscriptionSendNumber.setSubscriptionSend (send);
			subscriptionSendNumber.setNumber (sub.getNumber ());

			subscriptionSendNumberHelper.insert (
				subscriptionSendNumber);

			AffiliateRec affiliate =
				getAffiliateForSubscriptionSub (sub);

			if (templateVersion.getBilledEnabled ()) {

				// send the billed message

				MessageRec message =
					messageSender.get ()
						.number (sub.getNumber ())
						.messageText (billedMessageText)
						.numFrom (subscription.getBilledNumber ())
						.route (subscription.getBilledRoute ())
						.service (defaultService)
						.batch (batch)
						.affiliate (affiliate)
						.deliveryTypeCode ("subscription")
						.ref (subscriptionSendNumber.getId ())
						.send ();

				subscriptionSendNumber.setBilledMessage (message);
				subscriptionSendNumber.setThreadId (message.getThreadId ());
				subscriptionSendNumber.setSent (false);

			} else {

				// send the free messages

				for (TemplatePartRec tp
						: templateVersion.getTemplateParts ()) {

					MessageRec message =
						messageSender.get ()
							.threadId (subscriptionSendNumber.getThreadId ())
							.number (sub.getNumber ())
							.messageString (tp.getMessage ())
							.numFrom (subscription.getFreeNumber ())
							.route (subscription.getFreeRoute ())
							.service (defaultService)
							.batch (batch)
							.affiliate (affiliate)
							.send ();

					if (subscriptionSendNumber.getThreadId () == null)
						subscriptionSendNumber.setThreadId (
							message.getThreadId ());

				}

				subscriptionSendNumber.setSent (true);

			}

		}

	}

	public AffiliateRec getAffiliateForSubscriptionSub (
			SubscriptionSubRec subscriptionSub) {

		SubscriptionAffiliateRec subscriptionAffiliate =
			subscriptionSub.getSubscriptionAffiliate ();

		if (subscriptionAffiliate == null)
			return null;

		return affiliateHelper.findByCode(
			subscriptionAffiliate,
			"default");

	}

}
