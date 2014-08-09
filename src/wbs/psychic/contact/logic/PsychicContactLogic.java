package wbs.psychic.contact.logic;

import wbs.psychic.contact.model.PsychicContactRec;
import wbs.psychic.profile.model.PsychicProfileRec;
import wbs.psychic.user.core.model.PsychicUserRec;

public interface PsychicContactLogic {

	void sendProfile (
			PsychicUserRec user,
			Integer threadId);

	void sendProfile (
			PsychicUserRec user,
			PsychicProfileRec profile,
			Integer threadId);

	PsychicContactRec selectContact (
			PsychicUserRec user);

	PsychicContactRec findOrCreatePsychicContact (
			PsychicUserRec user,
			PsychicProfileRec profile);

}
