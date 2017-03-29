package wbs.smsapps.subscription.daemon;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;

import wbs.platform.send.GenericScheduleDaemon;
import wbs.platform.send.GenericSendHelper;

import wbs.smsapps.subscription.logic.SubscriptionSendHelper;
import wbs.smsapps.subscription.model.SubscriptionRec;
import wbs.smsapps.subscription.model.SubscriptionSendNumberRec;
import wbs.smsapps.subscription.model.SubscriptionSendRec;

@SingletonComponent ("subscriptionScheduleDaemon")
public
class SubscriptionScheduleDaemon
	extends
		GenericScheduleDaemon <
			SubscriptionRec,
			SubscriptionSendRec,
			SubscriptionSendNumberRec
		> {

	// singleton dependencies

	@SingletonDependency
	SubscriptionSendHelper subscriptionSendHelper;

	// implementation

	@Override
	protected
	GenericSendHelper <
		SubscriptionRec,
		SubscriptionSendRec,
		SubscriptionSendNumberRec
	> helper () {
		return subscriptionSendHelper;
	}

}
