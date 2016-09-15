package wbs.apn.chat.help.logic;

import com.google.common.base.Optional;

import wbs.apn.chat.help.model.ChatHelpLogRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.platform.user.model.UserRec;

public
interface ChatHelpLogic {

	void sendHelpMessage (
			UserRec user,
			ChatUserRec chatUser,
			String text,
			Optional<Long> threadId,
			Optional<ChatHelpLogRec> replyTo);

}