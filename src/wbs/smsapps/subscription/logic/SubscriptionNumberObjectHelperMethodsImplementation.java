package wbs.smsapps.subscription.logic;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.sms.number.core.model.NumberRec;

import wbs.smsapps.subscription.model.SubscriptionNumberObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionNumberObjectHelperMethods;
import wbs.smsapps.subscription.model.SubscriptionNumberRec;
import wbs.smsapps.subscription.model.SubscriptionRec;

public
class SubscriptionNumberObjectHelperMethodsImplementation
	implements SubscriptionNumberObjectHelperMethods {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@WeakSingletonDependency
	SubscriptionNumberObjectHelper subscriptionNumberHelper;

	// implementation

	@Override
	public
	SubscriptionNumberRec findOrCreate (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull SubscriptionRec subscription,
			@NonNull NumberRec number) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"findOrCreate");

		// find existing

		SubscriptionNumberRec subscriptionNumber =
			subscriptionNumberHelper.find (
				subscription,
				number);

		if (subscriptionNumber != null)
			return subscriptionNumber;

		// create new

		return subscriptionNumberHelper.insert (
			taskLogger,
			subscriptionNumberHelper.createInstance ()

			.setSubscription (
				subscription)

			.setNumber (
				number)

			.setActive (
				false)

		);

	}

}