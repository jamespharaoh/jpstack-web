package wbs.smsapps.subscription.logic;

import lombok.NonNull;

import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectHooks;

import wbs.smsapps.subscription.model.SubscriptionRec;
import wbs.smsapps.subscription.model.SubscriptionSendRec;

public
class SubscriptionSendHooks
	implements ObjectHooks<SubscriptionSendRec> {

	// implementation

	@Override
	public
	void beforeInsert (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull SubscriptionSendRec subscriptionSend) {

		SubscriptionRec subscription =
			subscriptionSend.getSubscription ();

		// set index

		subscriptionSend

			.setIndex (
				subscription.getNumSendsTotal ());

	}

	@Override
	public
	void afterInsert (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull SubscriptionSendRec subscriptionSend) {

		SubscriptionRec subscription =
			subscriptionSend.getSubscription ();

		// update parent counts

		subscription

			.setNumSendsTotal (
				subscription.getNumSendsTotal () + 1);

	}

}