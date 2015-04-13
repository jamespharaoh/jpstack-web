package wbs.clients.apn.chat.broadcast.logic;

import wbs.clients.apn.chat.user.core.model.ChatUserRec;

public
interface ChatBroadcastLogic {

	boolean canSendToUser (
			ChatUserRec chatUser,
			boolean includeBlocked,
			boolean includeOptedOut);

}
