package wbs.smsapps.subscription.console;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

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
	SubscriptionSubRec subscriptionSub;

	@Override
	public
	NumberRec getNumber () {
		return subscriptionSub.getSubscriptionNumber ().getNumber ();
	}

	@Override
	public
	Boolean getActive () {
		return subscriptionSub.getActive ();
	}

	@Override
	public
	Instant getStartTime () {
		return subscriptionSub.getStarted ();
	}

	@Override
	public
	Instant getEndTime () {
		return subscriptionSub.getEnded ();
	}

	@Override
	public
	Record<?> getParentObject () {

		return subscriptionSub
			.getSubscriptionNumber ()
			.getSubscription ();

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
			subscriptionSub
				.getSubscriptionNumber ()
				.getSubscription (),
			"manage",
			"admin");

	}

}
