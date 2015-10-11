package wbs.smsapps.subscription.model;

public
interface SubscriptionSendPartDaoMethods {

	SubscriptionSendPartRec find (
			SubscriptionSendRec subscriptionSend,
			SubscriptionListRec subscriptionList);

}