package wbs.smsapps.subscription.model;

import wbs.framework.database.Transaction;

import wbs.sms.number.core.model.NumberRec;

public
interface SubscriptionNumberObjectHelperMethods {

	SubscriptionNumberRec findOrCreate (
			Transaction parentTransaction,
			SubscriptionRec subscription,
			NumberRec number);

}