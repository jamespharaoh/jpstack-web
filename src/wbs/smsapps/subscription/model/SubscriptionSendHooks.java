package wbs.smsapps.subscription.model;

import wbs.framework.object.AbstractObjectHooks;

public
class SubscriptionSendHooks
	extends AbstractObjectHooks<SubscriptionSendRec> {

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
				(int) (long)
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