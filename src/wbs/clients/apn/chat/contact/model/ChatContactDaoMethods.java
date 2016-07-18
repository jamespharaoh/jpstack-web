package wbs.clients.apn.chat.contact.model;

import wbs.clients.apn.chat.user.core.model.ChatUserRec;

public
interface ChatContactDaoMethods {

	ChatContactRec findNoFlush (
			ChatUserRec fromChatUser,
			ChatUserRec toChatUser);

}