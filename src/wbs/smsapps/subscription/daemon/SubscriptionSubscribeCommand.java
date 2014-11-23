package wbs.smsapps.subscription.daemon;

import static wbs.framework.utils.etc.Misc.equal;

import java.util.Date;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.NonNull;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.Record;
import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.ReceivedMessage;
import wbs.sms.messageset.logic.MessageSetLogic;
import wbs.sms.number.core.model.NumberRec;
import wbs.smsapps.subscription.logic.SubscriptionLogic;
import wbs.smsapps.subscription.model.SubscriptionAffiliateRec;
import wbs.smsapps.subscription.model.SubscriptionNumberObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionNumberRec;
import wbs.smsapps.subscription.model.SubscriptionObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionRec;
import wbs.smsapps.subscription.model.SubscriptionSubObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionSubRec;

@PrototypeComponent ("subscriptionSubscribeCommand")
public
class SubscriptionSubscribeCommand
	implements CommandHandler {

	// dependencies

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
	SubscriptionNumberObjectHelper subscriptionNumberHelper;

	@Inject
	SubscriptionSubObjectHelper subscriptionSubHelper;

	@Inject
	SubscriptionLogic subscriptionLogic;

	// details

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"subscription.subscribe",
			"subscription_affiliate.subscribe"
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

		Record<?> commandParent =
			objectManager.getParent (
				command);

		SubscriptionRec subscription;
		SubscriptionAffiliateRec subscriptionAffiliate;

		String parentObjectTypeCode =
			command.getParentObjectType ().getCode ();

		if ((Object) commandParent instanceof SubscriptionRec) {

			subscription =
				(SubscriptionRec) (Object)
				commandParent;

			subscriptionAffiliate =
				null;

		} else if (equal (
				parentObjectTypeCode,
				"subscription_affiliate")) {

			subscriptionAffiliate =
				(SubscriptionAffiliateRec) (Object)
				commandParent;

			subscription =
				subscriptionAffiliate.getSubscription ();

		} else {

			throw new RuntimeException (
				commandParent.getClass ().getName ());

		}

		MessageRec message =
			messageHelper.find (
				receivedMessage.getMessageId ());

		NumberRec number =
			message.getNumber ();

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
				number);

		// if there was one send the "already subscribed" message set

		if (subscriptionSub != null) {

			AffiliateRec affiliate =
				subscriptionLogic.getAffiliateForSubscriptionSub (
					subscriptionSub);

			if (affiliate != null)
				receivedMessage.setAffiliateId (
					affiliate.getId ());

			messageSetLogic.sendMessageSet (
				messageSetLogic.findMessageSet (
					subscription,
					"subscription_subscribe_already"),
				message.getThreadId (),
				number,
				serviceHelper.findByCode (
					subscription,
					"default"),
				affiliate);

		} else {

			// look up the affiliate

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

			// find or create subscription number

			SubscriptionNumberRec subscriptionNumber =
				subscriptionNumberHelper.findOrCreate (
					subscription,
					number);

			// ok subscribe them

			subscriptionSubHelper.insert (
				new SubscriptionSubRec ()

				.setSubscriptionNumber (
					subscriptionNumber)

				.setActive (
					true)

				.setStarted (
					new Date ())

				.setSubscriptionAffiliate (
					subscriptionAffiliate)

			);

			// update the counter

			subscription
				.incNumSubscribers ();

			if (subscriptionAffiliate != null) {

				subscriptionAffiliate
					.setNumSubscribers (
						subscriptionAffiliate.getNumSubscribers () + 1);

			}

			// and send whatever messages

			ServiceRec defaultService =
				serviceHelper.findByCode (
					subscription,
					"default");

			messageSetLogic.sendMessageSet (
				messageSetLogic.findMessageSet (
					subscription,
					"subscription_subscribe_success"),
				message.getThreadId (),
				number,
				defaultService,
				affiliate);

		}

		transaction.commit ();

		return Status.processed;

	}

}
