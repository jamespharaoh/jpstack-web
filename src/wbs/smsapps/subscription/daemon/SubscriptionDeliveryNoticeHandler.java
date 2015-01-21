package wbs.smsapps.subscription.daemon;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.isNotNull;

import java.util.Arrays;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.delivery.daemon.DeliveryHandler;
import wbs.sms.message.delivery.model.DeliveryObjectHelper;
import wbs.sms.message.delivery.model.DeliveryRec;
import wbs.sms.message.outbox.logic.MessageSender;
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

	// dependencies

	@Inject
	Database database;

	@Inject
	DeliveryObjectHelper deliveryHelper;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	SubscriptionBillObjectHelper subscriptionBillHelper;

	@Inject
	SubscriptionLogic subscriptionLogic;

	// prototype dependencies

	@Inject
	Provider<MessageSender> messageSender;

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
			int deliveryId,
			Integer ref) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		DeliveryRec delivery =
			deliveryHelper.find (
				deliveryId);

		MessageRec message =
			delivery.getMessage ();

		SubscriptionBillRec subscriptionBill =
			subscriptionBillHelper.find (
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
				equal (
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
				equal (
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
