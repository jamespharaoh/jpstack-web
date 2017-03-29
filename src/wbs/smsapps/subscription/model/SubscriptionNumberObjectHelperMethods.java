package wbs.smsapps.subscription.model;

import wbs.framework.logging.TaskLogger;

import wbs.sms.number.core.model.NumberRec;

public
interface SubscriptionNumberObjectHelperMethods {

	SubscriptionNumberRec findOrCreate (
			TaskLogger parentTaskLogger,
			SubscriptionRec subscription,
			NumberRec number);

}