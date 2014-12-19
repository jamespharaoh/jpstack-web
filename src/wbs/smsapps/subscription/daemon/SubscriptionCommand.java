package wbs.smsapps.subscription.daemon;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.NonNull;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.Record;
import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.ReceivedMessage;
import wbs.sms.messageset.logic.MessageSetLogic;
import wbs.smsapps.subscription.logic.SubscriptionLogic;
import wbs.smsapps.subscription.model.SubscriptionAffiliateRec;
import wbs.smsapps.subscription.model.SubscriptionKeywordRec;
import wbs.smsapps.subscription.model.SubscriptionListRec;
import wbs.smsapps.subscription.model.SubscriptionNumberObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionRec;
import wbs.smsapps.subscription.model.SubscriptionSubObjectHelper;

@PrototypeComponent ("subscriptionCommand")
public
class SubscriptionCommand
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

	// state

	CommandRec command;

	SubscriptionRec subscription;
	SubscriptionAffiliateRec subscriptionAffiliate;
	SubscriptionKeywordRec subscriptionKeyword;
	SubscriptionListRec subscriptionList;

	// details

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {

			"subscription.subscribe",
			"subscription_affiliate.subscribe",
			"subscription_keyword.subscribe",
			"subscription_list.subscribe",

			"subscription.unsubscribe"

		};

	}

	// implementation

	@Override
	public
	Status handle (
			int commandId,
			@NonNull ReceivedMessage receivedMessage) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		findCommand (
			commandId);

		/*
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
		*/

		transaction.commit ();

		return CommandHandler.Status.processed;

	}

	void findCommand (
			int commandId) {

		commandHelper.find (
			commandId);

		Record<?> commandParent =
			objectManager.getParent (
				command);

		if (((Object) commandParent) instanceof SubscriptionRec) {

			subscription =
				(SubscriptionRec)
				(Object)
				commandParent;

		}

		if (((Object) commandParent) instanceof SubscriptionAffiliateRec) {

			subscriptionAffiliate =
				(SubscriptionAffiliateRec)
				(Object)
				commandParent;

			subscription =
				subscriptionAffiliate.getSubscription ();

		}

		if (((Object) commandParent) instanceof SubscriptionKeywordRec) {

			subscriptionKeyword =
				(SubscriptionKeywordRec)
				(Object)
				commandParent;

			subscription =
				subscriptionKeyword.getSubscription ();

		}

		if (((Object) commandParent) instanceof SubscriptionListRec) {

			subscriptionList =
				(SubscriptionListRec)
				(Object)
				commandParent;

			subscription =
				subscriptionList.getSubscription ();

		}

	}

}
