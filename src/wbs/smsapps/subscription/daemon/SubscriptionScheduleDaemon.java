package wbs.smsapps.subscription.daemon;

import javax.inject.Inject;

import lombok.extern.log4j.Log4j;

import org.apache.log4j.Logger;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.platform.send.GenericScheduleDaemon;
import wbs.platform.send.GenericSendHelper;
import wbs.smsapps.subscription.logic.SubscriptionSendHelper;
import wbs.smsapps.subscription.model.SubscriptionRec;
import wbs.smsapps.subscription.model.SubscriptionSendNumberRec;
import wbs.smsapps.subscription.model.SubscriptionSendRec;

@Log4j
@SingletonComponent ("subscriptionScheduleDaemon")
public
class SubscriptionScheduleDaemon
	extends
		GenericScheduleDaemon<
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

	@Override
	protected
	Logger log () {
		return log;
	}

}
