package wbs.apn.chat.contact.model;

import wbs.framework.database.Transaction;

import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatBlockDaoMethods {

	ChatBlockRec find (
			Transaction parentTransaction,
			ChatUserRec chatUser,
			ChatUserRec blockedChatUser);

}