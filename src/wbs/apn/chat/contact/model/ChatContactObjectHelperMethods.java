package wbs.apn.chat.contact.model;

import com.google.common.base.Optional;

import wbs.apn.chat.contact.model.ChatContactRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatContactObjectHelperMethods {

	Optional <ChatContactRec> find (
			ChatUserRec fromUser,
			ChatUserRec toUser);

	ChatContactRec findOrCreate (
			ChatUserRec fromUser,
			ChatUserRec toUser);

}