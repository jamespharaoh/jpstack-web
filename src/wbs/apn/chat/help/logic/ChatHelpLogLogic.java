package wbs.apn.chat.help.logic;

import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.help.model.ChatHelpLogRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.platform.user.model.UserRec;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageRec;

public interface ChatHelpLogLogic {

	ChatHelpLogRec createChatHelpLogIn (
			ChatUserRec chatUser,
			MessageRec message,
			String text,
			CommandRec command,
			boolean queue);

	ChatHelpLogRec createChatHelpLogOut (
			ChatUserRec chatUser,
			ChatHelpLogRec replyTo,
			UserRec user,
			MessageRec message,
			ChatMessageRec chatMessage,
			String text,
			CommandRec command);

}
