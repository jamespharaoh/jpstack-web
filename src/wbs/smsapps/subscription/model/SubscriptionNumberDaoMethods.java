package wbs.smsapps.subscription.model;

import java.util.List;

import wbs.sms.number.core.model.NumberRec;

public
interface SubscriptionNumberDaoMethods {

	SubscriptionNumberRec find (
			SubscriptionRec subscription,
			NumberRec number);

	List <Long> searchIds (
			SubscriptionNumberSearch search);

}