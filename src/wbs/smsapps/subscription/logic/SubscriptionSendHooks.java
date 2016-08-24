package wbs.smsapps.subscription.logic;

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
			SubscriptionSendRec subscriptionSend) {

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
			SubscriptionSendRec subscriptionSend) {

		SubscriptionRec subscription =
			subscriptionSend.getSubscription ();

		// update parent counts

		subscription

			.setNumSendsTotal (
				subscription.getNumSendsTotal () + 1);

	}

}