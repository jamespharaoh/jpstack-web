package wbs.smsapps.subscription.daemon;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.Record;
import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.core.logic.KeywordFinder;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;
import wbs.sms.message.outbox.logic.MessageSender;
import wbs.sms.messageset.logic.MessageSetLogic;
import wbs.sms.number.core.model.NumberRec;
import wbs.smsapps.subscription.logic.SubscriptionLogic;
import wbs.smsapps.subscription.model.SubscriptionAffiliateRec;
import wbs.smsapps.subscription.model.SubscriptionKeywordObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionKeywordRec;
import wbs.smsapps.subscription.model.SubscriptionListRec;
import wbs.smsapps.subscription.model.SubscriptionNumberObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionNumberRec;
import wbs.smsapps.subscription.model.SubscriptionObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionRec;
import wbs.smsapps.subscription.model.SubscriptionSubObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionSubRec;

import com.google.common.base.Optional;

@Accessors (fluent = true)
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
	EventLogic eventLogic;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	KeywordFinder keywordFinder;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	MessageSetLogic messageSetLogic;

	@Inject
	ObjectManager objectManager;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	SubscriptionKeywordObjectHelper subscriptionKeywordHelper;

	@Inject
	SubscriptionObjectHelper subscriptionHelper;

	@Inject
	SubscriptionNumberObjectHelper subscriptionNumberHelper;

	@Inject
	SubscriptionSubObjectHelper subscriptionSubHelper;

	@Inject
	SubscriptionLogic subscriptionLogic;

	// prototype dependencies

	@Inject
	Provider<MessageSender> messageSenderProvider;

	// properties

	@Getter @Setter
	InboxRec inbox;

	@Getter @Setter
	CommandRec command;

	@Getter @Setter
	Optional<Integer> commandRef;

	@Getter @Setter
	String rest;

	// state

	Transaction transaction;

	MessageRec message;
	NumberRec number;

	SubscriptionRec subscription;
	SubscriptionAffiliateRec subscriptionAffiliate;
	SubscriptionKeywordRec subscriptionKeyword;
	SubscriptionListRec subscriptionList;

	SubscriptionNumberRec subscriptionNumber;
	SubscriptionSubRec subscriptionSub;

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
	InboxAttemptRec handle () {

		transaction =
			database.currentTransaction ();

		findCommand ();

		message =
			inbox.getMessage ();

		number =
			message.getNumber ();

		subscriptionNumber =
			subscriptionNumberHelper.findOrCreate (
				subscription,
				number);

		if (
			equal (
				command.getCode (),
				"subscribe")
		) {

			return doSubscribe ();

		} else if (
			equal (
				command.getCode (),
				"unsubscribe")
		) {

			return doUnsubscribe ();

		} else {

			throw new RuntimeException (
				stringFormat (
					"Illegal command code: %s",
					command.getCode ()));

		}

	}

	void matchKeyword () {

		for (
			KeywordFinder.Match keywordMatch
				: keywordFinder.find (
					rest)
		) {

			SubscriptionKeywordRec subscriptionKeyword =
				subscriptionKeywordHelper.findByCode (
					subscription,
					keywordMatch.simpleKeyword ());

			subscriptionAffiliate =
				ifNull (
					subscriptionAffiliate,
					subscriptionKeyword.getSubscriptionAffiliate ());

			subscriptionList =
				ifNull (
					subscriptionList,
					subscriptionKeyword.getSubscriptionList ());

		}

	}

	InboxAttemptRec doSubscribe () {

		matchKeyword ();

		// set affiliate

		if (
			subscriptionAffiliate != null
			&& subscriptionNumber.getSubscriptionAffiliate () == null
		) {

			subscriptionNumber

				.setSubscriptionAffiliate (
					subscriptionAffiliate);

			eventLogic.createEvent (
				"subscription_number_affiliate",
				subscriptionNumber,
				subscriptionAffiliate,
				message);

		}

		// subscribe

		SubscriptionSubRec activeSubscriptionSub =
			subscriptionNumber.getActiveSubscriptionSub ();

		if (

			activeSubscriptionSub == null

			|| notEqual (
				activeSubscriptionSub.getSubscriptionList (),
				subscriptionList)

		) {

			if (activeSubscriptionSub != null) {

				// remove old sub

				activeSubscriptionSub

					.setActive (
						false)

					.setEndedThreadId (
						message.getThreadId ())

					.setEnded (
						transaction.now ());

				// update stats

				subscription

					.setNumSubscribers (
						subscription.getNumSubscribers () - 1);

				SubscriptionAffiliateRec activeSubscriptionAffiliate =
					activeSubscriptionSub.getSubscriptionAffiliate ();

				activeSubscriptionAffiliate

					.setNumSubscribers (
						activeSubscriptionAffiliate.getNumSubscribers () - 1);

				SubscriptionListRec activeSubscriptionList =
					activeSubscriptionSub.getSubscriptionList ();

				activeSubscriptionList

					.setNumSubscribers (
						activeSubscriptionList.getNumSubscribers () - 1);

			}

			// create new sub

			SubscriptionSubRec newSubscriptionSub =
				subscriptionSubHelper.insert (
					new SubscriptionSubRec ()

				.setSubscriptionNumber (
					subscriptionNumber)

				.setIndex (
					subscriptionNumber.getNumSubs ())

				.setSubscriptionList (
					subscriptionList)

				.setSubscriptionAffiliate (
					subscriptionAffiliate)

				.setStartedThreadId (
					message.getThreadId ())

				.setStarted (
					transaction.now ())

				.setActive (
					true)

			);

			// update number

			subscriptionNumber

				.setActive (
					true)

				.setActiveSubscriptionSub (
					newSubscriptionSub)

				.setSubscriptionList (
					subscriptionList)

				.setFirstJoin (
					ifNull (
						subscriptionNumber.getFirstJoin (),
						transaction.now ()))

				.setLastJoin (
					transaction.now ())

				.setNumSubs (
					subscriptionNumber.getNumSubs () + 1);

			// update stats

			subscription

				.setNumSubscribers (
					subscription.getNumSubscribers () + 1);

			subscriptionList

				.setNumSubscribers (
					subscriptionList.getNumSubscribers () + 1);

			subscriptionAffiliate

				.setNumSubscribers (
					subscriptionAffiliate.getNumSubscribers () + 1);

			// create event

			eventLogic.createEvent (
				"subscription_number_subscribe",
				subscriptionNumber,
				subscriptionList,
				message);

		}

		// send response

		MessageRec response =
			messageSenderProvider.get ()

			.threadId (
				message.getThreadId ())

			.number (
				message.getNumber ())

			.messageText (
				subscription.getSubscribeMessageText ())

			.numFrom (
				subscription.getFreeNumber ())

			.routerResolve (
				subscription.getFreeRouter ())

			.service (
				serviceHelper.findByCode (
					subscriptionList,
					"default"))

			.affiliate (
				affiliateHelper.findByCode (
					subscriptionAffiliate,
					"default"))

			.send ();

		// process message

		return inboxLogic.inboxProcessed (
			inbox,
			Optional.of (response.getService ()),
			Optional.of (response.getAffiliate ()),
			command);

	}

	InboxAttemptRec doUnsubscribe () {

		throw new RuntimeException ("TODO");

	}

	void findCommand () {

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
