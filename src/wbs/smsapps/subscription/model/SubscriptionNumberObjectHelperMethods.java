package wbs.smsapps.subscription.model;

import wbs.sms.number.core.model.NumberRec;

public
interface SubscriptionNumberObjectHelperMethods {

	SubscriptionNumberRec findOrCreate (
			SubscriptionRec subscription,
			NumberRec number);

}