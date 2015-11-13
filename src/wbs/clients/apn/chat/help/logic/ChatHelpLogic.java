package wbs.clients.apn.chat.help.logic;

import com.google.common.base.Optional;

import wbs.clients.apn.chat.help.model.ChatHelpLogRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.platform.user.model.UserRec;

public
interface ChatHelpLogic {

	void sendHelpMessage (
			UserRec user,
			ChatUserRec chatUser,
			String text,
			Optional<Integer> threadId,
			Optional<ChatHelpLogRec> replyTo);

}