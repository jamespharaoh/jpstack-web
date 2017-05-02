package wbs.apn.chat.help.logic;

import com.google.common.base.Optional;

import wbs.framework.database.Transaction;

import wbs.platform.user.model.UserRec;

import wbs.apn.chat.help.model.ChatHelpLogRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatHelpLogic {

	void sendHelpMessage (
			Transaction parentTransaction,
			UserRec user,
			ChatUserRec chatUser,
			String text,
			Optional <Long> threadId,
			Optional <ChatHelpLogRec> replyTo);

}