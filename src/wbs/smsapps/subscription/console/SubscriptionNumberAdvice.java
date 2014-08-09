package wbs.smsapps.subscription.console;

import java.util.Date;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;
import wbs.platform.priv.console.PrivChecker;
import wbs.sms.number.core.console.NumberPlugin;
import wbs.sms.number.core.model.NumberRec;
import wbs.smsapps.subscription.model.SubscriptionSubRec;

@Accessors (chain = true)
@PrototypeComponent ("subscriptionNumberAdvice")
public
class SubscriptionNumberAdvice
	implements NumberPlugin.Link {

	@Inject
	PrivChecker privChecker;

	@Getter @Setter
	SubscriptionNumberLinkProvider provider;

	@Getter @Setter
	SubscriptionSubRec sub;

	@Override
	public
	NumberRec getNumber () {
		return sub.getNumber ();
	}

	@Override
	public
	Boolean getActive () {
		return sub.getActive ();
	}

	@Override
	public
	Date getStartTime () {
		return sub.getStarted ();
	}

	@Override
	public
	Date getEndTime () {
		return sub.getEnded ();
	}

	@Override
	public
	Record<?> getParentObject () {
		return sub.getSubscription ();
	}

	@Override
	public
	Record<?> getSubscriptionObject () {
		return null;
	}

	@Override
	public
	String getType () {
		return "subscription";
	}

	@Override
	public
	boolean canView () {

		return privChecker.can (
			sub.getSubscription (),
			"manage",
			"admin");

	}

}
