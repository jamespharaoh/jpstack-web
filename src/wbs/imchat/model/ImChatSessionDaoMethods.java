package wbs.imchat.model;

import wbs.framework.database.Transaction;

public
interface ImChatSessionDaoMethods {

	ImChatSessionRec findBySecret (
			Transaction parentTransaction,
			String secret);

}