package wbs.imchat.model;

import lombok.Data;

import wbs.platform.user.model.UserRec;

@Data
public
class ImChatMessageUserStats {

	ImChatRec imChat;
	UserRec senderUser;

	Long numMessages;
	Long numCharacters;

}
