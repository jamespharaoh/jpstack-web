package wbs.smsapps.subscription.console;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;

import wbs.sms.number.core.console.NumberPlugin;
import wbs.sms.number.core.model.NumberRec;

import wbs.smsapps.subscription.model.SubscriptionSubRec;

@Accessors (chain = true)
@PrototypeComponent ("subscriptionNumberAdvice")
public
class SubscriptionNumberAdvice
	implements NumberPlugin.Link {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserPrivChecker privChecker;

	// properties

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
	boolean canView (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"canView");

		) {

			return privChecker.canRecursive (
				transaction,
				subscriptionSub
					.getSubscriptionNumber ()
					.getSubscription (),
				"manage",
				"admin");

		}

	}

}
