package wbs.apn.chat.contact.model;

import lombok.Data;

import wbs.apn.chat.core.model.ChatRec;

@Data
public
class ChatMessageStats {

	ChatRec chat;

	Long numMessages;
	Long numMessagesIn;
	Long numMessagesOut;

	Long numCharacters;
	Long numCharactersIn;
	Long numCharactersOut;

	Long numMessagesFinal;
	Long numMessagesFinalIn;
	Long numMessagesFinalOut;

}
