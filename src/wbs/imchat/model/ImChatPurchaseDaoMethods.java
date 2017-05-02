package wbs.imchat.model;

import wbs.framework.database.Transaction;

public
interface ImChatPurchaseDaoMethods {

	ImChatPurchaseRec findByToken (
			Transaction parentTransaction,
			String token);

}