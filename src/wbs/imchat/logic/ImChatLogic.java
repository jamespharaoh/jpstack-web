package wbs.imchat.logic;

import com.google.common.base.Optional;

import wbs.framework.database.Transaction;

import wbs.platform.user.model.UserRec;

import wbs.imchat.model.ImChatConversationRec;
import wbs.imchat.model.ImChatCustomerRec;

public
interface ImChatLogic {

	void conversationEnd (
			Transaction parentTransaction,
			ImChatConversationRec conversation);

	void conversationEmailSend (
			Transaction parentTransaction,
			ImChatConversationRec conversation);

	void customerPasswordGenerate (
			Transaction parentTransaction,
			ImChatCustomerRec customer,
			Optional <UserRec> consoleUser);

}
