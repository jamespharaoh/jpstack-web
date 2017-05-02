package wbs.apn.chat.contact.model;

import com.google.common.base.Optional;

import wbs.framework.database.Transaction;

import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatContactDaoMethods {

	Optional <ChatContactRec> findNoCache (
			Transaction parentTransaction,
			ChatUserRec fromChatUser,
			ChatUserRec toChatUser);

}