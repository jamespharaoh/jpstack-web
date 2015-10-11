package wbs.smsapps.subscription.model;

import java.util.List;

import org.joda.time.Instant;

public
interface SubscriptionSendDaoMethods {

	List<SubscriptionSendRec> findSending ();

	List<SubscriptionSendRec> findScheduled (
			Instant now);

}