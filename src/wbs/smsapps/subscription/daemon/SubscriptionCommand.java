package wbs.smsapps.subscription.daemon;

import static wbs.utils.etc.LogicUtils.referenceNotEqualWithClass;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.OptionalUtils.optionalFromJava;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectManager;

import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.service.model.ServiceObjectHelper;

import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.core.logic.KeywordFinder;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;
import wbs.sms.message.outbox.logic.SmsMessageSender;
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

@Accessors (fluent = true)
@PrototypeComponent ("subscriptionCommand")
public
class SubscriptionCommand
	implements CommandHandler {

	// singleton dependencies

	@SingletonDependency
	AffiliateObjectHelper affiliateHelper;

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	@SingletonDependency
	KeywordFinder keywordFinder;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	SubscriptionKeywordObjectHelper subscriptionKeywordHelper;

	@SingletonDependency
	SubscriptionObjectHelper subscriptionHelper;

	@SingletonDependency
	SubscriptionNumberObjectHelper subscriptionNumberHelper;

	@SingletonDependency
	SubscriptionSubObjectHelper subscriptionSubHelper;

	@SingletonDependency
	SubscriptionLogic subscriptionLogic;

	// prototype dependencies

	@PrototypeDependency
	Provider <SmsMessageSender> messageSenderProvider;

	// properties

	@Getter @Setter
	InboxRec inbox;

	@Getter @Setter
	CommandRec command;

	@Getter @Setter
	Optional<Long> commandRef;

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
	InboxAttemptRec handle (
			@NonNull TaskLogger parentTaskLogger) {

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
			stringEqualSafe (
				command.getCode (),
				"subscribe")
		) {

			return doSubscribe ();

		} else if (
			stringEqualSafe (
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

		Optional<SubscriptionKeywordRec> subscriptionKeywordOptional =
			optionalFromJava (

			keywordFinder.find (
				rest)

			.stream ()

			.map (match ->
				subscriptionKeywordHelper.findByCode (
					subscription,
					match.simpleKeyword ()))

			.filter (
				Optional::isPresent)

			.map (
				Optional::get)

			.findFirst ()

		);

		if (
			optionalIsNotPresent (
				subscriptionKeywordOptional)
		) {
			return;
		}

		SubscriptionKeywordRec subscriptionKeyword =
			subscriptionKeywordOptional.get ();

		subscriptionAffiliate =
			ifNull (
				subscriptionAffiliate,
				subscriptionKeyword.getSubscriptionAffiliate ());

		subscriptionList =
			ifNull (
				subscriptionList,
				subscriptionKeyword.getSubscriptionList ());

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

			|| referenceNotEqualWithClass (
				SubscriptionListRec.class,
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
					subscriptionSubHelper.createInstance ()

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
				serviceHelper.findByCodeRequired (
					subscriptionList,
					"default"))

			.affiliate (
				affiliateHelper.findByCodeRequired (
					subscriptionAffiliate,
					"default"))

			.send ();

		// process message

		return smsInboxLogic.inboxProcessed (
			inbox,
			Optional.of (
				response.getService ()),
			Optional.of (
				response.getAffiliate ()),
			command);

	}

	InboxAttemptRec doUnsubscribe () {

		throw new RuntimeException ("TODO");

	}

	void findCommand () {

		Record <?> commandParent =
			objectManager.getParentRequired (
				command);

		if (commandParent instanceof SubscriptionRec) {

			subscription =
				(SubscriptionRec)
				commandParent;

		}

		if (commandParent instanceof SubscriptionAffiliateRec) {

			subscriptionAffiliate =
				(SubscriptionAffiliateRec)
				commandParent;

			subscription =
				subscriptionAffiliate.getSubscription ();

		}

		if (commandParent instanceof SubscriptionKeywordRec) {

			subscriptionKeyword =
				(SubscriptionKeywordRec)
				commandParent;

			subscription =
				subscriptionKeyword.getSubscription ();

		}

		if (((Object) commandParent) instanceof SubscriptionListRec) {

			subscriptionList =
				(SubscriptionListRec)
				commandParent;

			subscription =
				subscriptionList.getSubscription ();

		}

	}

}
