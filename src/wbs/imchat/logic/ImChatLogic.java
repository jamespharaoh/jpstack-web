package wbs.imchat.logic;

import com.google.common.base.Optional;

import wbs.imchat.model.ImChatConversationRec;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.platform.user.model.UserRec;

public
interface ImChatLogic {

	void conversationEnd (
			ImChatConversationRec conversation);

	void conversationEmailSend (
			ImChatConversationRec conversation);

	void customerPasswordGenerate (
			ImChatCustomerRec customer,
			Optional<UserRec> consoleUser);

}
