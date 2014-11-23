package wbs.smsapps.subscription.logic;

import wbs.platform.affiliate.model.AffiliateRec;
import wbs.smsapps.subscription.model.SubscriptionSubRec;

public
interface SubscriptionLogic {

	AffiliateRec getAffiliateForSubscriptionSub (
			SubscriptionSubRec subscriptionSub);

}
