package wbs.applications.imchat.logic;

import wbs.applications.imchat.model.ImChatConversationRec;

public
interface ImChatLogic {

	void conversationEnd (
			ImChatConversationRec conversation);

	void conversationEmailSend (
			ImChatConversationRec conversation);

}
