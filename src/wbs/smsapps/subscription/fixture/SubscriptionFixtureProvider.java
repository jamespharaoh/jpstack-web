package wbs.smsapps.subscription.fixture;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuObjectHelper;
import wbs.platform.menu.model.MenuRec;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionAffiliateObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionAffiliateRec;
import wbs.smsapps.subscription.model.SubscriptionKeywordObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionKeywordRec;
import wbs.smsapps.subscription.model.SubscriptionListObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionListRec;
import wbs.smsapps.subscription.model.SubscriptionObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionRec;

@PrototypeComponent ("subscriptionFixtureProvider")
public
class SubscriptionFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	MenuObjectHelper menuHelper;

	@Inject
	SliceObjectHelper sliceHelper;

	@Inject
	SubscriptionAffiliateObjectHelper subscriptionAffiliateHelper;

	@Inject
	SubscriptionObjectHelper subscriptionHelper;

	@Inject
	SubscriptionKeywordObjectHelper subscriptionKeywordHelper;

	@Inject
	SubscriptionListObjectHelper subscriptionListHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		menuHelper.insert (
			new MenuRec ()

			.setMenuGroup (
				menuGroupHelper.findByCode (
					GlobalId.root,
					"facility"))

			.setCode (
				"subscription")

			.setLabel (
				"Subscription")

			.setPath (
				"/subscriptions")

		);

		SubscriptionRec subscription =
			subscriptionHelper.insert (
				new SubscriptionRec ()

			.setSlice (
				sliceHelper.findByCode (
					GlobalId.root,
					"test"))

			.setCode (
				"test")

			.setName (
				"Test")

			.setDescription (
				"Test subscription")

		);

		for (
			int index = 0;
			index < 3;
			index ++
		) {

			SubscriptionListRec list =
				subscriptionListHelper.insert (
					new SubscriptionListRec ()

				.setSubscription (
					subscription)

				.setCode (
					stringFormat (
						"list_%s",
						index))

				.setName (
					stringFormat (
						"List %s",
						index))

				.setDescription (
					stringFormat (
						"Test subscription list %s",
						index))

			);

			SubscriptionAffiliateRec affiliate =
				subscriptionAffiliateHelper.insert (
					new SubscriptionAffiliateRec ()

				.setSubscription (
					subscription)

				.setCode (
					stringFormat (
						"affiliate_%s",
						index))

				.setName (
					stringFormat (
						"Affiliate %s",
						index))

				.setDescription (
					stringFormat (
						"Test subscription affiliate %s",
						index))

			);

			subscriptionKeywordHelper.insert (
				new SubscriptionKeywordRec ()

				.setSubscription (
					subscription)

				.setKeyword (
					stringFormat (
						"join%s",
						index))

				.setDescription (
					stringFormat (
						"Test subscription keyword join%s",
						index))

				.setSubscriptionAffiliate (
					affiliate)

				.setSubscriptionList (
					list)

			);

		}

	}

}
