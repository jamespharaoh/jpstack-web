package wbs.applications.imchat.model;

import java.util.List;

public
interface ImChatConversationDaoMethods {

	List<ImChatConversationRec> findPendingEmailLimit (
			int maxResults);

}
