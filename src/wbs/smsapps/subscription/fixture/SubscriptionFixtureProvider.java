package wbs.smsapps.subscription.fixture;

import static wbs.framework.utils.etc.CodeUtils.simplifyToCodeRequired;

import java.util.Map;

import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.keyword.model.KeywordObjectHelper;
import wbs.sms.keyword.model.KeywordSetObjectHelper;
import wbs.sms.keyword.model.KeywordSetRec;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.router.model.RouterObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionAffiliateObjectHelper;
import wbs.smsapps.subscription.model.SubscriptionAffiliateRec;
import wbs.smsapps.subscription.model.SubscriptionKeywordObjectHelper;
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
	Database database;

	@Inject
	KeywordObjectHelper keywordHelper;

	@Inject
	KeywordSetObjectHelper keywordSetHelper;

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	MenuItemObjectHelper menuItemHelper;

	@Inject
	RouteObjectHelper routeHelper;

	@Inject
	RouterObjectHelper routerHelper;

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

	@Inject
	TextObjectHelper textHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		menuItemHelper.insert (
			menuItemHelper.createInstance ()

			.setMenuGroup (
				menuGroupHelper.findByCodeRequired (
					GlobalId.root,
					"test",
					"facility"))

			.setCode (
				"subscription")

			.setName (
				"Subscription")

			.setDescription (
				"")

			.setLabel (
				"Subscription")

			.setTargetPath (
				"/subscriptions")

			.setTargetFrame (
				"main")

		);

		SubscriptionRec subscription =
			subscriptionHelper.insert (
				subscriptionHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCodeRequired (
					GlobalId.root,
					"test"))

			.setCode (
				"test")

			.setName (
				"Test")

			.setDescription (
				"Test subscription")

			.setBilledRoute (
				routeHelper.findByCodeRequired (
					GlobalId.root,
					"test",
					"bill"))

			.setBilledNumber (
				"bill")

			.setBilledMessage (
				textHelper.findOrCreate (
					"Billed message"))

			.setFreeRouter (
				routerHelper.findByCodeRequired (
					routeHelper.findByCodeRequired (
						GlobalId.root,
						"test",
						"free"),
					"static"))

			.setFreeNumber (
				"free")

			.setCreditsPerBill (
				2l)

			.setDebitsPerSend (
				1l)

			.setSubscribeMessageText (
				textHelper.findOrCreate (
					"Subsription confirmed"))

			.setUnsubscribeMessageText (
				textHelper.findOrCreate (
					"Subscription cancelled"))

		);

		database.flush ();

		KeywordSetRec inboundKeywordSet =
			keywordSetHelper.findByCodeRequired (
				GlobalId.root,
				"test",
				"inbound");

		keywordHelper.insert (
			keywordHelper.createInstance ()

			.setKeywordSet (
				inboundKeywordSet)

			.setKeyword (
				"sub")

			.setDescription (
				"Subscription subscribe")

			.setCommand (
				commandHelper.findByCodeRequired (
					subscription,
					"subscribe"))

		);

		keywordHelper.insert (
			keywordHelper.createInstance ()

			.setKeywordSet (
				inboundKeywordSet)

			.setKeyword (
				"unsub")

			.setDescription (
				"Subscription unsubscribe")

			.setCommand (
				commandHelper.findByCodeRequired (
					subscription,
					"unsubscribe"))

		);

		for (
			Map.Entry<String,String> listSpecEntry
				: listSpecs.entrySet ()
		) {

			SubscriptionListRec list =
				subscriptionListHelper.insert (
					subscriptionListHelper.createInstance ()

				.setSubscription (
					subscription)

				.setCode (
					simplifyToCodeRequired (
						listSpecEntry.getValue ()))

				.setName (
					listSpecEntry.getValue ())

				.setDescription (
					listSpecEntry.getValue ())

			);

			subscriptionKeywordHelper.insert (
				subscriptionKeywordHelper.createInstance ()

				.setSubscription (
					subscription)

				.setKeyword (
					listSpecEntry.getKey ())

				.setDescription (
					listSpecEntry.getValue ())

				.setSubscriptionList (
					list)

			);

		}

		for (
			Map.Entry<String,String> affiliateSpecEntry
				: affiliateSpecs.entrySet ()
		) {

			SubscriptionAffiliateRec affiliate =
				subscriptionAffiliateHelper.insert (
					subscriptionAffiliateHelper.createInstance ()

				.setSubscription (
					subscription)

				.setCode (
					simplifyToCodeRequired (
						affiliateSpecEntry.getValue ()))

				.setName (
					affiliateSpecEntry.getValue ())

				.setDescription (
					affiliateSpecEntry.getValue ())

			);

			database.flush ();

			keywordHelper.insert (
				keywordHelper.createInstance ()

				.setKeywordSet (
					inboundKeywordSet)

				.setKeyword (
					affiliateSpecEntry.getKey ())

				.setDescription (
					affiliateSpecEntry.getValue ())

				.setCommand (
					commandHelper.findByCodeRequired (
						affiliate,
						"subscribe"))

			);

		}

	}

	Map<String,String> affiliateSpecs =
		ImmutableMap.<String,String>builder ()
			.put ("jt", "Justin Toper")
			.put ("liv", "Psychic living")
			.put ("gts", "Gone Too Soon")
			.put ("sm", "Sally Morgan")
			.build ();

	Map<String,String> listSpecs =
		ImmutableMap.<String,String>builder ()
			.put ("ari", "Aries")
			.put ("tau", "Taurus")
			.put ("gem", "Gemini")
			.put ("leo", "Leo")
			.put ("vir", "Virgo")
			.put ("lib", "Libra")
			.put ("sco", "Scorpio")
			.put ("sag", "Saggitarius")
			.put ("cap", "Capricorn")
			.put ("aqu", "Aquarius")
			.put ("pic", "Pisces")
			.put ("can", "Cancer")
			.build ();

}