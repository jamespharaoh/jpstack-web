package wbs.smsapps.subscription.logic;

import static wbs.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.utils.etc.EnumUtils.enumNotInSafe;
import static wbs.utils.etc.NullUtils.ifNull;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

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

	@ClassSingletonDependency
	LogContext logContext;

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
	ComponentProvider <SmsMessageSender> messageSenderProvider;

	// implementation

	@Override
	public
	void sendNow (
			@NonNull Transaction parentTransaction,
			@NonNull SubscriptionSendNumberRec subscriptionSendNumber) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendNow");

		) {

			SubscriptionSendRec subscriptionSend =
				subscriptionSendNumber.getSubscriptionSend ();

			SubscriptionRec subscription =
				subscriptionSend.getSubscription ();

			SubscriptionNumberRec subscriptionNumber =
				subscriptionNumberHelper.find (
					transaction,
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
					transaction,
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
				messageSenderProvider.provide (
					transaction)

				.number (
					subscriptionNumber.getNumber ())

				.messageText (
					subscriptionSendPart.getText ())

				.numFrom (
					subscription.getFreeNumber ())

				.routerResolve (
					transaction,
					subscription.getFreeRouter ())

				.service (
					serviceHelper.findByCodeRequired (
						transaction,
						subscriptionList,
						"default"))

				.batch (
					subscriptionSend.getBatch ())

				.affiliate (
					affiliateHelper.findByCodeRequired (
						transaction,
						subscriptionAffiliate,
						"default"))

				.send (
					transaction);

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

	}

	@Override
	public
	void sendLater (
			@NonNull Transaction parentTransaction,
			@NonNull SubscriptionSendNumberRec subscriptionSendNumber) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sendLater");

		) {

			SubscriptionSendRec subscriptionSend =
				subscriptionSendNumber.getSubscriptionSend ();

			SubscriptionRec subscription =
				subscriptionSend.getSubscription ();

			SubscriptionNumberRec subscriptionNumber =
				subscriptionNumberHelper.find (
					transaction,
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
						transaction,
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
					messageSenderProvider.provide (
						transaction)

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
							transaction,
							ifNull (
								subscriptionList,
								subscription),
							"default"))

					.affiliate (
						affiliateHelper.findByCodeRequired (
							transaction,
							ifNull (
								subscriptionAffiliate,
								subscription),
							"default"))

					.deliveryTypeCode (
						transaction,
						"subscription")

					.ref (
						subscriptionBill.getId ())

					.send (
						transaction);

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

	}

	@Override
	public
	void scheduleSend (
			@NonNull Transaction parentTransaction,
			@NonNull SubscriptionSendRec subscriptionSend,
			@NonNull Instant scheduleForTime,
			@NonNull UserRec user) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"scheduleSend");

		) {

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
				transaction,
				"subscription_send_scheduled",
				user,
				subscriptionSend,
				transaction.now ());

		}

	}

}