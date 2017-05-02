package wbs.smsapps.subscription.logic;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectHooks;

import wbs.smsapps.subscription.model.SubscriptionRec;
import wbs.smsapps.subscription.model.SubscriptionSendRec;

public
class SubscriptionSendHooks
	implements ObjectHooks <SubscriptionSendRec> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	void beforeInsert (
			@NonNull Transaction parentTransaction,
			@NonNull SubscriptionSendRec subscriptionSend) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"beforeInsert");

		) {

			SubscriptionRec subscription =
				subscriptionSend.getSubscription ();

			// set index

			subscriptionSend

				.setIndex (
					subscription.getNumSendsTotal ());

		}

	}

	@Override
	public
	void afterInsert (
			@NonNull Transaction parentTransaction,
			@NonNull SubscriptionSendRec subscriptionSend) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"afterInsert");

		) {

			SubscriptionRec subscription =
				subscriptionSend.getSubscription ();

			// update parent counts

			subscription

				.setNumSendsTotal (
					subscription.getNumSendsTotal () + 1);

		}

	}

}