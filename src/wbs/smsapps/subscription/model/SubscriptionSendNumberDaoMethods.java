package wbs.smsapps.subscription.model;

import java.util.List;

public
interface SubscriptionSendNumberDaoMethods {

	List<SubscriptionSendNumberRec> findQueuedLimit (
			SubscriptionSendRec subscriptionSend,
			int maxResults);

}