package wbs.psychic.bill.daemon;

import java.util.Collection;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.psychic.bill.logic.PsychicBillLogic;
import wbs.psychic.bill.model.PsychicUserAccountRec;
import wbs.psychic.user.core.model.PsychicUserObjectHelper;
import wbs.psychic.user.core.model.PsychicUserRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.delivery.daemon.DeliveryHandler;
import wbs.sms.message.delivery.model.DeliveryObjectHelper;
import wbs.sms.message.delivery.model.DeliveryRec;

import com.google.common.collect.ImmutableList;

@PrototypeComponent ("psychicBillDeliveryHandler")
public
class PsychicBillDeliveryHandler
	implements DeliveryHandler {

	@Inject
	Database database;

	@Inject
	DeliveryObjectHelper deliveryHelper;

	@Inject
	PsychicBillLogic psychicBillLogic;

	@Inject
	PsychicUserObjectHelper psychicUserHelper;

	@Override
	public
	Collection<String> getDeliveryTypeCodes () {

		return ImmutableList.<String>of (
			"psychic_bill");

	}

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

		PsychicUserRec psychicUser =
			psychicUserHelper.find (
				message.getRef ());

		PsychicUserAccountRec account =
			psychicUser.getAccount ();

		// subtract credit based on old status

		if (delivery.getOldMessageStatus ().isPending ()) {

			account.setCreditPending (
				+ account.getCreditPending ()
				- message.getCharge ());

		}

		if (delivery.getOldMessageStatus ().isBadType ()) {

			account.setCreditFailure (
				+ account.getCreditFailure ()
				- message.getCharge ());

		}

		if (delivery.getOldMessageStatus ().isGoodType ()) {

			account.setCreditSuccess (
				+ account.getCreditSuccess ()
				- message.getCharge ());

		}

		// add credit based on new status

		if (delivery.getNewMessageStatus ().isPending ()) {

			account.setCreditPending (
				+ account.getCreditPending ()
				+ message.getCharge ());

		}

		if (delivery.getNewMessageStatus ().isBadType ()) {

			account.setCreditFailure (
				+ account.getCreditFailure ()
				+ message.getCharge ());

		}

		if (delivery.getNewMessageStatus ().isGoodType ()) {

			account.setCreditSuccess (
				+ account.getCreditSuccess ()
				+ message.getCharge ());

		}

		// if we just got a successful delivery invoke the credit system so it
		// can send another billed message, if it needs to

		if (! delivery.getOldMessageStatus ().isGoodType ()
			&& delivery.getNewMessageStatus ().isGoodType ()) {

			psychicBillLogic.creditCheck (
				psychicUser,
				0,
				true);

		}

		// wrap up

		deliveryHelper.remove (
			delivery);

		transaction.commit ();

	}

}
