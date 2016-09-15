package wbs.imchat.model;

import wbs.imchat.model.ImChatPurchaseRec;

public
interface ImChatPurchaseDaoMethods {

	ImChatPurchaseRec findByToken (
			String token);

}