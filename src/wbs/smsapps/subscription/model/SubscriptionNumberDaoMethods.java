package wbs.smsapps.subscription.model;

import java.util.List;

import wbs.framework.database.Transaction;

import wbs.sms.number.core.model.NumberRec;

public
interface SubscriptionNumberDaoMethods {

	SubscriptionNumberRec find (
			Transaction parentTransaction,
			SubscriptionRec subscription,
			NumberRec number);

	List <Long> searchIds (
			Transaction parentTransaction,
			SubscriptionNumberSearch search);

}