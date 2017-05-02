package wbs.smsapps.subscription.model;

import wbs.framework.database.Transaction;

public
interface SubscriptionSendPartDaoMethods {

	SubscriptionSendPartRec find (
			Transaction parentTransaction,
			SubscriptionSendRec subscriptionSend,
			SubscriptionListRec subscriptionList);

}