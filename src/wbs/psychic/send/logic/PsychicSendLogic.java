package wbs.psychic.send.logic;

import wbs.psychic.user.core.model.PsychicUserRec;
import wbs.sms.message.core.model.MessageRec;

public interface PsychicSendLogic {

	void sendCharges (
			PsychicUserRec user,
			Integer threadId);

	public MessageRec sendWelcome (
			PsychicUserRec user,
			Integer threadId);

	public MessageRec sendMagic (
			PsychicUserRec user,
			String commandCode,
			int magicRef,
			Integer threadId,
			String message,
			String serviceCode);

}
