package wbs.applications.imchat.logic;

import com.google.common.base.Optional;

import wbs.applications.imchat.model.ImChatConversationRec;
import wbs.applications.imchat.model.ImChatCustomerRec;
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
