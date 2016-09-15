package wbs.apn.chat.contact.model;

import com.google.common.base.Optional;

import wbs.apn.chat.contact.model.ChatContactRec;
import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatContactDaoMethods {

	Optional <ChatContactRec> findNoCache (
			ChatUserRec fromChatUser,
			ChatUserRec toChatUser);

}