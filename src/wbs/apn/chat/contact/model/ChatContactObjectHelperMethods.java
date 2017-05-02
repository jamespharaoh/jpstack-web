package wbs.apn.chat.contact.model;

import com.google.common.base.Optional;

import wbs.framework.database.Transaction;

import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatContactObjectHelperMethods {

	Optional <ChatContactRec> find (
			Transaction parentTransaction,
			ChatUserRec fromUser,
			ChatUserRec toUser);

	ChatContactRec findOrCreate (
			Transaction parentTransaction,
			ChatUserRec fromUser,
			ChatUserRec toUser);

}