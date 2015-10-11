package wbs.clients.apn.chat.contact.model;

import wbs.clients.apn.chat.user.core.model.ChatUserRec;

public
interface ChatContactObjectHelperMethods {

	ChatContactRec findOrCreate (
			ChatUserRec fromUser,
			ChatUserRec toUser);

}