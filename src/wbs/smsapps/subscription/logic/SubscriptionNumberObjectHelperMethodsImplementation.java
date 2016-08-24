package wbs.smsapps.subscription.logic;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;

import wbs.sms.number.core.model.NumberRec;
import wbs.smsapps.subscription.model.SubscriptionNumberObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionNumberObjectHelperMethods;
import wbs.smsapps.subscription.model.SubscriptionNumberRec;
import wbs.smsapps.subscription.model.SubscriptionRec;

public
class SubscriptionNumberObjectHelperMethodsImplementation
	implements SubscriptionNumberObjectHelperMethods {

	// indirect dependencies

	@Inject
	Provider<SubscriptionNumberObjectHelper>
	subscriptionNumberHelperProvider;

	// implementation

	@Override
	public
	SubscriptionNumberRec findOrCreate (
			@NonNull SubscriptionRec subscription,
			@NonNull NumberRec number) {

		SubscriptionNumberObjectHelper subscriptionNumberHelper =
			subscriptionNumberHelperProvider.get ();

		// find existing

		SubscriptionNumberRec subscriptionNumber =
			subscriptionNumberHelper.find (
				subscription,
				number);

		if (subscriptionNumber != null)
			return subscriptionNumber;

		// create new

		return subscriptionNumberHelper.insert (
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