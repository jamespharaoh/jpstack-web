package wbs.applications.imchat.model;

public
interface ImChatSessionDaoMethods {

	ImChatSessionRec findBySecret (
			String secret);

}