package wbs.apn.chat.contact.model;

import lombok.Data;

import wbs.platform.user.model.UserRec;

import wbs.apn.chat.core.model.ChatRec;

@Data
public
class ChatMessageUserStats {

	ChatRec chat;
	UserRec user;

	Long numMessages;
	Long numFinalMessages;
	Long numCharacters;

}
