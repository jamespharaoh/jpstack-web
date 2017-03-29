package wbs.smsapps.subscription.logic;

import org.joda.time.Instant;

import wbs.framework.logging.TaskLogger;

import wbs.platform.user.model.UserRec;

import wbs.smsapps.subscription.model.SubscriptionSendNumberRec;
import wbs.smsapps.subscription.model.SubscriptionSendRec;

public
interface SubscriptionLogic {

	void sendNow (
			TaskLogger parentTaskLogger,
			SubscriptionSendNumberRec subscriptionSendNumber);

	void sendLater (
			TaskLogger parentTaskLogger,
			SubscriptionSendNumberRec subscriptionSendNumber);

	void scheduleSend (
			TaskLogger parentTaskLogger,
			SubscriptionSendRec subscriptionSend,
			Instant scheduleForTime,
			UserRec user);

}
