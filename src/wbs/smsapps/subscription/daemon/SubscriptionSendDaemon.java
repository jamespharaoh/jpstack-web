package wbs.smsapps.subscription.daemon;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.send.GenericSendDaemon;
import wbs.platform.send.GenericSendHelper;
import wbs.smsapps.subscription.logic.SubscriptionSendHelper;
import wbs.smsapps.subscription.model.SubscriptionRec;
import wbs.smsapps.subscription.model.SubscriptionSendNumberRec;
import wbs.smsapps.subscription.model.SubscriptionSendRec;

@SingletonComponent ("subscriptionSendDaemon")
public
class SubscriptionSendDaemon
	extends
		GenericSendDaemon<
			SubscriptionRec,
			SubscriptionSendRec,
			SubscriptionSendNumberRec
		> {

	// dependencies

	@Inject
	SubscriptionSendHelper subscriptionSendHelper;

	// implementation

	@Override
	protected
	GenericSendHelper<
		SubscriptionRec,
		SubscriptionSendRec,
		SubscriptionSendNumberRec
	> helper () {

		return subscriptionSendHelper;

	}

}
