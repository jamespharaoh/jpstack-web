package wbs.smsapps.subscription.model;

import java.util.List;

import org.joda.time.Instant;

import wbs.framework.database.Transaction;

public
interface SubscriptionSendDaoMethods {

	List <SubscriptionSendRec> findSending (
			Transaction parentTransaction);

	List <SubscriptionSendRec> findScheduled (
			Transaction parentTransaction,
			Instant now);

}