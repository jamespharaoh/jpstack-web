package wbs.smsapps.subscription.logic;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.smsapps.subscription.model.SubscriptionAffiliateRec;
import wbs.smsapps.subscription.model.SubscriptionSubRec;

@SingletonComponent ("subscriptionLogic")
public
class SubscriptionLogicImpl
	implements SubscriptionLogic {

	// dependencies

	@Inject
	AffiliateObjectHelper affiliateHelper;

	// implementation

	public
	AffiliateRec getAffiliateForSubscriptionSub (
			SubscriptionSubRec subscriptionSub) {

		SubscriptionAffiliateRec subscriptionAffiliate =
			subscriptionSub.getSubscriptionAffiliate ();

		if (subscriptionAffiliate == null)
			return null;

		return affiliateHelper.findByCode(
			subscriptionAffiliate,
			"default");

	}

}
