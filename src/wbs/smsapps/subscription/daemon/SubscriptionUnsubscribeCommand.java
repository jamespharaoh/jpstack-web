package wbs.smsapps.subscription.daemon;

import java.util.Date;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.NonNull;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.ReceivedMessage;
import wbs.sms.messageset.logic.MessageSetLogic;
import wbs.smsapps.subscription.model.SubscriptionAffiliateRec;
import wbs.smsapps.subscription.model.SubscriptionObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionRec;
import wbs.smsapps.subscription.model.SubscriptionSubObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionSubRec;

@SingletonComponent ("subscriptionUnsubscribeCommand")
public
class SubscriptionUnsubscribeCommand
	implements CommandHandler {

	@Inject
	AffiliateObjectHelper affiliateHelper;

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	Database database;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	MessageSetLogic messageSetLogic;

	@Inject
	ObjectManager objectManager;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	SubscriptionObjectHelper subscriptionHelper;

	@Inject
	SubscriptionSubObjectHelper subscriptionSubHelper;

	@Override
	public String[] getCommandTypes () {

		return new String [] {
			"subscription.unsubscribe"
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

		SubscriptionRec subscription =
			(SubscriptionRec) (Object)
			objectManager.getParent (
				command);

		MessageRec message =
			messageHelper.find (
				receivedMessage.getMessageId ());

		// lock the subscription to prevent concurrent updates

		subscriptionHelper.lock (
			subscription);

		// set service on received message

		receivedMessage.setServiceId (
			serviceHelper.findByCode (subscription, "default").getId ());

		// check for a pre-existing subscription

		SubscriptionSubRec subscriptionSub =
			subscriptionSubHelper.findActive (
				subscription,
				message.getNumber ());

		// if there was one...

		if (subscriptionSub != null) {

			SubscriptionAffiliateRec subscriptionAffiliate =
				subscriptionSub.getSubscriptionAffiliate ();

			AffiliateRec affiliate =
				subscriptionAffiliate != null
					? affiliateHelper.findByCode (
						subscriptionAffiliate,
						"default")
					: null;

			if (affiliate != null) {

				receivedMessage.setAffiliateId (
					affiliate.getId ());

			}

			// unsubscribe them

			subscriptionSub
				.setActive (false)
				.setEnded (new Date ());

			// update the counter

			subscription.decNumSubscribers ();

			if (subscriptionAffiliate != null) {

				subscriptionAffiliate
					.setNumSubscribers (
						subscriptionAffiliate.getNumSubscribers () - 1);

			}

			// and send whatever messages

			messageSetLogic.sendMessageSet (
				messageSetLogic.findMessageSet (
					subscription,
					"subscription_unsubscribe_success"),
				message.getThreadId (),
				message.getNumber (),
				serviceHelper.findByCode (
					subscription,
					"default"),
				affiliate);

		} else {

			// they aren't subscribed, just send messages

			messageSetLogic.sendMessageSet (
				messageSetLogic.findMessageSet (
					subscription,
					"subscription_unsubscribe_already"),
				message.getThreadId (),
				message.getNumber (),
				serviceHelper.findByCode (
					subscription,
					"default"));

		}

		transaction.commit ();

		return CommandHandler.Status.processed;

	}

}
