package wbs.apn.chat.broadcast.logic;

import wbs.framework.database.Transaction;

import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatBroadcastLogic {

	boolean canSendToUser (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			boolean includeBlocked,
			boolean includeOptedOut);

}
