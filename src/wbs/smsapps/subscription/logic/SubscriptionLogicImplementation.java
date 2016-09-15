package wbs.smsapps.subscription.logic;

import static wbs.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.utils.etc.EnumUtils.enumNotInSafe;
import static wbs.utils.etc.NullUtils.ifNull;

import javax.inject.Provider;

import org.joda.time.Instant;

import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.logic.SmsMessageSender;
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
class SubscriptionLogicImplementation
	implements SubscriptionLogic {

	// singleton dependencies

	@SingletonDependency
	AffiliateObjectHelper affiliateHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	SubscriptionBillObjectHelper subscriptionBillHelper;

	@SingletonDependency
	SubscriptionNumberObjectHelper subscriptionNumberHelper;

	@SingletonDependency
	SubscriptionSendPartObjectHelper subscriptionSendPartHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <SmsMessageSender> messageSenderProvider;

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
			enumNotInSafe (
				subscriptionSendNumber.getState (),
				SubscriptionSendNumberState.queued,
				SubscriptionSendNumberState.pendingBill)
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
				serviceHelper.findByCodeRequired (
					subscriptionList,
					"default"))

			.batch (
				subscriptionSend.getBatch ())

			.affiliate (
				affiliateHelper.findByCodeRequired (
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
			enumNotEqualSafe (
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

			SubscriptionBillRec subscriptionBill =
				subscriptionBillHelper.insert (
					subscriptionBillHelper.createInstance ()

				.setSubscriptionNumber (
					subscriptionNumber)

				.setIndex (
					subscriptionNumber.getNumBills ())

				.setCreatedTime (
					transaction.now ())

				.setState (
					SubscriptionBillState.pending)

			);

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
					serviceHelper.findByCodeRequired (
						ifNull (
							subscriptionList,
							subscription),
						"default"))

				.affiliate (
					affiliateHelper.findByCodeRequired (
						ifNull (
							subscriptionAffiliate,
							subscription),
						"default"))

				.deliveryTypeCode (
					"subscription")

				.ref (
					subscriptionBill.getId ())

				.send ();

			subscriptionBill

				.setMessage (
					billedMessage);

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
			transaction.now ());

	}

}