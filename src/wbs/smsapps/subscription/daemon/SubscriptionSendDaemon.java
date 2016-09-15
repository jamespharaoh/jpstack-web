package wbs.smsapps.subscription.daemon;

import lombok.extern.log4j.Log4j;

import org.apache.log4j.Logger;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.send.GenericSendDaemon;
import wbs.platform.send.GenericSendHelper;
import wbs.smsapps.subscription.logic.SubscriptionSendHelper;
import wbs.smsapps.subscription.model.SubscriptionRec;
import wbs.smsapps.subscription.model.SubscriptionSendNumberRec;
import wbs.smsapps.subscription.model.SubscriptionSendRec;

@Log4j
@SingletonComponent ("subscriptionSendDaemon")
public
class SubscriptionSendDaemon
	extends
		GenericSendDaemon <
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
	GenericSendHelper<
		SubscriptionRec,
		SubscriptionSendRec,
		SubscriptionSendNumberRec
	> helper () {

		return subscriptionSendHelper;

	}

	@Override
	protected
	Logger log () {

		return log;

	}

}
