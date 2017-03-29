package wbs.apn.chat.broadcast.logic;

import wbs.framework.logging.TaskLogger;

import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatBroadcastLogic {

	boolean canSendToUser (
			TaskLogger parentTaskLogger,
			ChatUserRec chatUser,
			boolean includeBlocked,
			boolean includeOptedOut);

}
