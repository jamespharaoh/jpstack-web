package wbs.imchat.model;

import java.util.List;

import wbs.imchat.model.ImChatConversationRec;

public
interface ImChatConversationDaoMethods {

	List<ImChatConversationRec> findPendingEmailLimit (
			int maxResults);

}
