package wbs.imchat.model;

import java.util.List;

import wbs.framework.database.Transaction;

public
interface ImChatConversationDaoMethods {

	List <ImChatConversationRec> findPendingEmailLimit (
			Transaction parentTransaction,
			Long maxResults);

}
