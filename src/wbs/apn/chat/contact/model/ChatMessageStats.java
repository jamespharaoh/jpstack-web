package wbs.apn.chat.contact.model;

import lombok.Data;

import wbs.apn.chat.core.model.ChatRec;

@Data
public
class ChatMessageStats {

	ChatRec chat;

	Long numMessages;
	Long numFinalMessages;
	Long numCharacters;

}
