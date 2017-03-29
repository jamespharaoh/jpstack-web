package wbs.apn.chat.contact.model;

import com.google.common.base.Optional;

import wbs.framework.logging.TaskLogger;

import wbs.apn.chat.user.core.model.ChatUserRec;

public
interface ChatContactObjectHelperMethods {

	Optional <ChatContactRec> find (
			ChatUserRec fromUser,
			ChatUserRec toUser);

	ChatContactRec findOrCreate (
			TaskLogger parentTaskLogger,
			ChatUserRec fromUser,
			ChatUserRec toUser);

}