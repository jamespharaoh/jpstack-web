package wbs.clients.apn.chat.contact.model;

import wbs.clients.apn.chat.user.core.model.ChatUserRec;

public
interface ChatBlockDaoMethods {

	ChatBlockRec find (
			ChatUserRec chatUser,
			ChatUserRec blockedChatUser);

}