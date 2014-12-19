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
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.keyword.model.KeywordObjectHelper;
import wbs.sms.keyword.model.KeywordRec;
import wbs.sms.keyword.model.KeywordSetObjectHelper;
import wbs.sms.keyword.model.KeywordSetRec;
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
	CommandObjectHelper commandHelper;

	@Inject
	KeywordObjectHelper keywordHelper;

	@Inject
	KeywordSetObjectHelper keywordSetHelper;

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

		KeywordSetRec inboundKeywordSet =
			keywordSetHelper.findByCode (
				GlobalId.root,
				"test",
				"inbound");

		keywordHelper.insert (
			new KeywordRec ()

			.setKeywordSet (
				inboundKeywordSet)

			.setKeyword (
				"sub")

			.setDescription (
				"Subscription subscribe")

			.setCommand (
				commandHelper.findByCode (
					subscription,
					"subscribe"))

		);

		keywordHelper.insert (
			new KeywordRec ()

			.setKeywordSet (
				inboundKeywordSet)

			.setKeyword (
				"unsub")

			.setDescription (
				"Subscription unsubscribe")

			.setCommand (
				commandHelper.findByCode (
					subscription,
					"unsubscribe"))

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
