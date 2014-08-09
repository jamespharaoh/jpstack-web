package wbs.psychic.user.core.logic;

import wbs.psychic.core.model.PsychicRec;
import wbs.psychic.user.core.model.PsychicUserRec;
import wbs.sms.number.core.model.NumberRec;

public
interface PsychicUserLogic {

	PsychicUserRec findOrCreateUser (
			PsychicRec psychic,
			NumberRec number);

	void join (
			PsychicUserRec psychicUser,
			Integer threadId);

}
