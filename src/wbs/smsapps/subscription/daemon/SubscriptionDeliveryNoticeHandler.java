package wbs.smsapps.subscription.daemon;

import static wbs.utils.etc.LogicUtils.referenceNotEqualWithClass;
import static wbs.utils.etc.Misc.isNotNull;

import java.util.Arrays;
import java.util.Collection;

import javax.inject.Provider;

import lombok.Cleanup;
import lombok.NonNull;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.delivery.daemon.DeliveryHandler;
import wbs.sms.message.delivery.model.DeliveryObjectHelper;
import wbs.sms.message.delivery.model.DeliveryRec;
import wbs.sms.message.outbox.logic.SmsMessageSender;
import wbs.smsapps.subscription.logic.SubscriptionLogic;
import wbs.smsapps.subscription.model.SubscriptionBillObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionBillRec;
import wbs.smsapps.subscription.model.SubscriptionBillState;
import wbs.smsapps.subscription.model.SubscriptionNumberRec;
import wbs.smsapps.subscription.model.SubscriptionRec;

@PrototypeComponent ("subscriptionDeliveryNoticeHandler")
public
class SubscriptionDeliveryNoticeHandler
	implements DeliveryHandler {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	DeliveryObjectHelper deliveryHelper;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	SubscriptionBillObjectHelper subscriptionBillHelper;

	@SingletonDependency
	SubscriptionLogic subscriptionLogic;

	// prototype dependencies

	@PrototypeDependency
	Provider <SmsMessageSender> messageSenderProvider;

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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long deliveryId,
			@NonNull Long ref) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"SubscriptionDeliveryNoticeHandler.handle (deliveryId, ref)",
				this);

		DeliveryRec delivery =
			deliveryHelper.findRequired (
				deliveryId);

		MessageRec message =
			delivery.getMessage ();

		SubscriptionBillRec subscriptionBill =
			subscriptionBillHelper.findRequired (
				message.getRef ());

		SubscriptionNumberRec subscriptionNumber =
			subscriptionBill.getSubscriptionNumber ();

		SubscriptionRec subscription =
			subscriptionNumber.getSubscription ();

		if (
			subscriptionBill.getState () == SubscriptionBillState.pending
			&& delivery.getNewMessageStatus ().isBadType ()
		) {

			// delivery failure

			subscriptionBill

				.setState (
					SubscriptionBillState.failed);

			if (
				referenceNotEqualWithClass (
					SubscriptionBillRec.class,
					subscriptionNumber.getPendingSubscriptionBill (),
					subscriptionBill)
			) {

				subscriptionNumber

					.setPendingSubscriptionBill (
						null);

			}

		} else if (
			subscriptionBill.getState () != SubscriptionBillState.delivered
			&& delivery.getNewMessageStatus ().isGoodType ()
		) {

			// delivery success

			subscriptionBill

				.setState (
					SubscriptionBillState.delivered)

				.setDeliveredTime (
					transaction.now ());

			subscriptionNumber

				.setBalance (
					subscriptionNumber.getBalance ()
					+ subscription.getCreditsPerBill ());

			if (
				referenceNotEqualWithClass (
					SubscriptionBillRec.class,
					subscriptionNumber.getPendingSubscriptionBill (),
					subscriptionBill)
			) {

				subscriptionNumber

					.setPendingSubscriptionBill (
						null);

			}

			// send pending

			if (

				subscriptionNumber.getBalance ()
					> subscription.getDebitsPerSend ()

				&& isNotNull (
					subscriptionNumber.getPendingSubscriptionSendNumber ())

			) {

				subscriptionLogic.sendNow (
					subscriptionNumber.getPendingSubscriptionSendNumber ());

			}

		} else {

			// nothing to do

		}

		// clean up

		deliveryHelper.remove (
			delivery);

		transaction.commit ();

	}

}
