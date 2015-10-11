package wbs.applications.imchat.model;

public
interface ImChatPurchaseDaoMethods {

	ImChatPurchaseRec findByToken (
			String token);

}