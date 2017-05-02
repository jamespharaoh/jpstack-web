package wbs.smsapps.subscription.logic;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

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
			@NonNull Transaction parentTransaction,
			@NonNull SubscriptionRec subscription,
			@NonNull NumberRec number) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findOrCreate");

		) {

			// find existing

			SubscriptionNumberRec subscriptionNumber =
				subscriptionNumberHelper.find (
					transaction,
					subscription,
					number);

			if (subscriptionNumber != null)
				return subscriptionNumber;

			// create new

			return subscriptionNumberHelper.insert (
				transaction,
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

}