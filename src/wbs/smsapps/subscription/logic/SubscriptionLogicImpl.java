package wbs.smsapps.subscription.logic;

import static wbs.framework.utils.etc.Misc.notEqual;

import javax.inject.Inject;
import javax.inject.Provider;

import org.joda.time.Instant;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.logic.MessageSender;
import wbs.smsapps.subscription.model.SubscriptionAffiliateRec;
import wbs.smsapps.subscription.model.SubscriptionBillObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionBillRec;
import wbs.smsapps.subscription.model.SubscriptionBillState;
import wbs.smsapps.subscription.model.SubscriptionListRec;
import wbs.smsapps.subscription.model.SubscriptionNumberObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionNumberRec;
import wbs.smsapps.subscription.model.SubscriptionRec;
import wbs.smsapps.subscription.model.SubscriptionSendNumberRec;
import wbs.smsapps.subscription.model.SubscriptionSendNumberState;
import wbs.smsapps.subscription.model.SubscriptionSendPartObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionSendPartRec;
import wbs.smsapps.subscription.model.SubscriptionSendRec;
import wbs.smsapps.subscription.model.SubscriptionSendState;
import wbs.smsapps.subscription.model.SubscriptionSubRec;

@SingletonComponent ("subscriptionLogic")
public
class SubscriptionLogicImpl
	implements SubscriptionLogic {

	// dependencies

	@Inject
	AffiliateObjectHelper affiliateHelper;

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	SubscriptionBillObjectHelper subscriptionBillHelper;

	@Inject
	SubscriptionNumberObjectHelper subscriptionNumberHelper;

	@Inject
	SubscriptionSendPartObjectHelper subscriptionSendPartHelper;

	@Inject
	TimeFormatter timeFormatter;

	// prototype dependencies

	@Inject
	Provider<MessageSender> messageSenderProvider;

	// implementation

	@Override
	public
	void sendNow (
			SubscriptionSendNumberRec subscriptionSendNumber) {

		SubscriptionSendRec subscriptionSend =
			subscriptionSendNumber.getSubscriptionSend ();

		SubscriptionRec subscription =
			subscriptionSend.getSubscription ();

		SubscriptionNumberRec subscriptionNumber =
			subscriptionNumberHelper.find (
				subscription,
				subscriptionSendNumber.getNumber ());

		SubscriptionSubRec subscriptionSub =
			subscriptionSendNumber.getSubscriptionSub ();

		SubscriptionListRec subscriptionList =
			subscriptionSub.getSubscriptionList ();

		SubscriptionAffiliateRec subscriptionAffiliate =
			subscriptionSub.getSubscriptionAffiliate ();

		SubscriptionSendPartRec subscriptionSendPart =
			subscriptionSendPartHelper.find (
				subscriptionSend,
				subscriptionList);

		// sanity check

		if (
			notEqual (
				subscriptionSendNumber.getState (),
				SubscriptionSendNumberState.queued)
		) {
			throw new IllegalStateException ();
		}

		if (
			subscriptionNumber.getBalance ()
				< subscription.getDebitsPerSend ()
		) {
			throw new IllegalStateException ();
		}

		// send message

		MessageRec freeMessage =
			messageSenderProvider.get ()

			.number (
				subscriptionNumber.getNumber ())

			.messageText (
				subscriptionSendPart.getText ())

			.numFrom (
				subscription.getFreeNumber ())

			.routerResolve (
				subscription.getFreeRouter ())

			.service (
				serviceHelper.findByCode (
					subscriptionList,
					"default"))

			.batch (
				subscriptionSend.getBatch ())

			.affiliate (
				affiliateHelper.findByCode (
					subscriptionAffiliate,
					"default"))

			.send ();

		// update state

		subscriptionSendNumber

			.setState (
				SubscriptionSendNumberState.sent)

			.setMessage (
				freeMessage);

		subscriptionNumber

			.setBalance (
				+ subscriptionNumber.getBalance ()
				- subscription.getDebitsPerSend ());
	}

	@Override
	public
	void sendLater (
			SubscriptionSendNumberRec subscriptionSendNumber) {

		Transaction transaction =
			database.currentTransaction ();

		SubscriptionSendRec subscriptionSend =
			subscriptionSendNumber.getSubscriptionSend ();

		SubscriptionRec subscription =
			subscriptionSend.getSubscription ();

		SubscriptionNumberRec subscriptionNumber =
			subscriptionNumberHelper.find (
				subscription,
				subscriptionSendNumber.getNumber ());

		SubscriptionListRec subscriptionList =
			subscriptionNumber.getSubscriptionList ();

		SubscriptionAffiliateRec subscriptionAffiliate =
			subscriptionNumber.getSubscriptionAffiliate ();

		// sanity check

		if (
			notEqual (
				subscriptionSendNumber.getState (),
				SubscriptionSendNumberState.queued)
		) {
			throw new IllegalStateException ();
		}

		if (
			subscriptionNumber.getBalance ()
				>= subscription.getDebitsPerSend ()
		) {
			throw new IllegalStateException ();
		}

		// cancel old pending message

		SubscriptionSendNumberRec oldSubscriptionSendNumber =
			subscriptionNumber.getPendingSubscriptionSendNumber ();

		if (oldSubscriptionSendNumber != null) {

			oldSubscriptionSendNumber

				.setState (
					SubscriptionSendNumberState.skipped);

			subscriptionNumber

				.setPendingSubscriptionSendNumber (
					null);

		}

		// set new pending message

		subscriptionSendNumber

			.setState (
				SubscriptionSendNumberState.pendingBill);

		subscriptionNumber

			.setPendingSubscriptionSendNumber (
				subscriptionSendNumber);

		// send billed message

		if (
			subscriptionNumber.getPendingSubscriptionBill () == null
		) {

			// send bill

			MessageRec billedMessage =
				messageSenderProvider.get ()

				.number (
					subscriptionNumber.getNumber ())

				.messageText (
					subscription.getBilledMessage ())

				.numFrom (
					subscription.getBilledNumber ())

				.route (
					subscription.getBilledRoute ())

				.service (
					serviceHelper.findByCode (
						subscriptionList,
						"default"))

				.affiliate (
					affiliateHelper.findByCode (
						subscriptionAffiliate,
						"default"))

				.deliveryTypeCode (
					"subscription")

				.ref (
					subscriptionSendNumber.getId ())

				.send ();

			SubscriptionBillRec subscriptionBill =
				subscriptionBillHelper.insert (
					new SubscriptionBillRec ()

				.setSubscriptionNumber (
					subscriptionNumber)

				.setIndex (
					subscriptionNumber.getNumBills ())

				.setMessage (
					billedMessage)

				.setCreatedTime (
					transaction.now ())

				.setState (
					SubscriptionBillState.pending)

			);

			// update state

			subscriptionNumber

				.setPendingSubscriptionBill (
					subscriptionBill)

				.setNumBills (
					subscriptionNumber.getNumBills () + 1);

		}

	}

	@Override
	public
	void scheduleSend (
			SubscriptionSendRec subscriptionSend,
			Instant scheduleForTime,
			UserRec user) {

		Transaction transaction =
			database.currentTransaction ();

		// sanity check

		if (
			subscriptionSend.getState ()
				!= SubscriptionSendState.notSent
		) {
			throw new IllegalStateException ();
		}

		if (
			scheduleForTime.isBefore (
				transaction.now ())
		) {
			throw new IllegalArgumentException ();
		}

		// update send

		subscriptionSend

			.setSentUser (
				user)

			.setScheduledTime (
				transaction.now ())

			.setScheduledForTime (
				scheduleForTime)

			.setState (
				SubscriptionSendState.scheduled);

		// create event

		eventLogic.createEvent (
			"subscription_send_scheduled",
			user,
			subscriptionSend,
			timeFormatter.instantToTimestampString (
				timeFormatter.defaultTimezone (),
				transaction.now ()));

	}

}