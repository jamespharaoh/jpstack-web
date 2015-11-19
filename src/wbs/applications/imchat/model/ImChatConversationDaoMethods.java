package wbs.applications.imchat.model;

import java.util.List;

public
interface ImChatConversationDaoMethods {

	List<ImChatConversationRec> findPendingEmail (
			int maxResults);

}
