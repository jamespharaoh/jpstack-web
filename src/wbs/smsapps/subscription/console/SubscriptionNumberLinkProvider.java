package wbs.smsapps.subscription.console;

import java.util.Collections;
import java.util.List;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

import wbs.sms.number.core.console.NumberPlugin;
import wbs.sms.number.core.model.NumberRec;

import wbs.smsapps.subscription.model.SubscriptionSubObjectHelper;

@SingletonComponent ("subscriptionNumberLinkProvider")
public
class SubscriptionNumberLinkProvider
	implements NumberPlugin {

	// singleton dependencies

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	SubscriptionSubObjectHelper subscriptionSubHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <SubscriptionNumberAdvice> subscriptionNumberAdviceProvider;

	// details

	@Override
	public
	String getName () {
		return "subscription";
	}

	// implementation

	@Override
	public
	List <Link> findLinks (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull NumberRec number,
			boolean active) {

		/*
		// find relevant subs

		Map<String,Object> searchMap =
			new LinkedHashMap<String,Object> ();

		searchMap.put (
			"numberId",
			number.getId ());

		if (active)
			searchMap.put("active", true);

		List<SubscriptionSubRec> subscriptionSubs =
			subscriptionSubHelper.search (
				searchMap);

		// create advices

		List<Link> advices =
			new ArrayList<Link> ();

		for (SubscriptionSubRec subscriptionSub
				: subscriptionSubs) {

			SubscriptionNumberAdvice advice =
				subscriptionNumberAdvice.get ()
					.setProvider (this)
					.setSub (subscriptionSub);

			advices.add (advice);

		}

		return advices;
		*/

		return Collections.emptyList ();

	}

}
