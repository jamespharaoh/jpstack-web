package wbs.imchat.model;

import wbs.imchat.model.ImChatSessionRec;

public
interface ImChatSessionDaoMethods {

	ImChatSessionRec findBySecret (
			String secret);

}