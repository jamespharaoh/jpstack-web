package wbs.psychic.bill.logic;

import wbs.psychic.user.core.model.PsychicUserRec;
import wbs.sms.message.core.model.MessageRec;

public
interface PsychicBillLogic {

	CreditResult creditCheck (
			PsychicUserRec psychicUser,
			int amount,
			boolean chargeNow);

	MessageRec sendBilledMessage (
			PsychicUserRec psyhicUser);

	boolean chargeOneRequest (
			PsychicUserRec psychicUser,
			Integer threadId);

	void addInitialCredit (
			PsychicUserRec psychicUser);

	static
	enum CreditResult {
		passed,
		failed,
		stopped,
		barred,
		prepay,
		limit,
		daily;
	}

}
