package wbs.clients.apn.chat.help.logic;

import wbs.clients.apn.chat.help.model.ChatHelpLogRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.platform.user.model.UserRec;

public interface ChatHelpLogic {

	void sendHelpMessage (
			UserRec user,
			ChatUserRec chatUser,
			String text,
			Integer threadId,
			ChatHelpLogRec replyTo);

}