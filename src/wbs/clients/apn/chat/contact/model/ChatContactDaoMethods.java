package wbs.clients.apn.chat.contact.model;

import wbs.clients.apn.chat.user.core.model.ChatUserRec;

public
interface ChatContactDaoMethods {

	ChatContactRec find (
			ChatUserRec fromChatUser,
			ChatUserRec toChatUser);

}