package wbs.smsapps.subscription.logic;

import org.joda.time.Instant;

import wbs.platform.user.model.UserRec;
import wbs.smsapps.subscription.model.SubscriptionSendNumberRec;
import wbs.smsapps.subscription.model.SubscriptionSendRec;

public
interface SubscriptionLogic {

	void sendNow (
			SubscriptionSendNumberRec subscriptionSendNumber);

	void sendLater (
			SubscriptionSendNumberRec subscriptionSendNumber);

	void scheduleSend (
			SubscriptionSendRec subscriptionSend,
			Instant scheduleForTime,
			UserRec user);

}
