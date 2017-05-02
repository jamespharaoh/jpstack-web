package wbs.smsapps.subscription.logic;

import org.joda.time.Instant;

import wbs.framework.database.Transaction;

import wbs.platform.user.model.UserRec;

import wbs.smsapps.subscription.model.SubscriptionSendNumberRec;
import wbs.smsapps.subscription.model.SubscriptionSendRec;

public
interface SubscriptionLogic {

	void sendNow (
			Transaction parentTransaction,
			SubscriptionSendNumberRec subscriptionSendNumber);

	void sendLater (
			Transaction parentTransaction,
			SubscriptionSendNumberRec subscriptionSendNumber);

	void scheduleSend (
			Transaction parentTransaction,
			SubscriptionSendRec subscriptionSend,
			Instant scheduleForTime,
			UserRec user);

}
