package wbs.smsapps.subscription.model;

import java.util.List;

import wbs.framework.database.Transaction;

public
interface SubscriptionSendNumberDaoMethods {

	List <SubscriptionSendNumberRec> findQueuedLimit (
			Transaction parentTransaction,
			SubscriptionSendRec subscriptionSend,
			Long maxResults);

}