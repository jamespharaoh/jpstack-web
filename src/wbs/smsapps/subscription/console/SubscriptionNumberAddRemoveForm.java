package wbs.smsapps.subscription.console;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.sms.number.format.model.NumberFormatRec;
import wbs.smsapps.subscription.model.SubscriptionAffiliateRec;
import wbs.smsapps.subscription.model.SubscriptionListRec;

@Accessors (fluent = true)
@Data
public
class SubscriptionNumberAddRemoveForm {

	NumberFormatRec numberFormat;

	SubscriptionAffiliateRec subscriptionAffiliate;
	SubscriptionListRec subscriptionList;

	String numbers;

}
