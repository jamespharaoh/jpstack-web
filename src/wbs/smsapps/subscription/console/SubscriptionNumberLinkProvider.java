package wbs.smsapps.subscription.console;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.priv.PrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.sms.number.core.console.NumberPlugin;
import wbs.sms.number.core.model.NumberRec;
import wbs.smsapps.subscription.model.SubscriptionSubObjectHelper;

@SingletonComponent ("subscriptionNumberLinkProvider")
public
class SubscriptionNumberLinkProvider
	implements NumberPlugin {

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	PrivChecker privChecker;

	@Inject
	SubscriptionSubObjectHelper subscriptionSubHelper;

	@Inject
	Provider<SubscriptionNumberAdvice> subscriptionNumberAdvice;

	@Override
	public
	String getName () {
		return "subscription";
	}

	@Override
	public
	List<Link> findLinks (
			NumberRec number,
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
