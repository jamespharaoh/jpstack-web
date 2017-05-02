package wbs.smsapps.subscription.fixture;

import static wbs.utils.string.CodeUtils.simplifyToCodeRequired;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;

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

	// singleton dependencies

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	KeywordObjectHelper keywordHelper;

	@SingletonDependency
	KeywordSetObjectHelper keywordSetHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
	RouteObjectHelper routeHelper;

	@SingletonDependency
	RouterObjectHelper routerHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	@SingletonDependency
	SubscriptionAffiliateObjectHelper subscriptionAffiliateHelper;

	@SingletonDependency
	SubscriptionObjectHelper subscriptionHelper;

	@SingletonDependency
	SubscriptionKeywordObjectHelper subscriptionKeywordHelper;

	@SingletonDependency
	SubscriptionListObjectHelper subscriptionListHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	// implementation

	@Override
	public
	void createFixtures (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createFixtures");

		) {

			menuItemHelper.insert (
				transaction,
				menuItemHelper.createInstance ()

				.setMenuGroup (
					menuGroupHelper.findByCodeRequired (
						transaction,
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
					transaction,
					subscriptionHelper.createInstance ()

				.setSlice (
					sliceHelper.findByCodeRequired (
						transaction,
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
						transaction,
						GlobalId.root,
						"test",
						"bill"))

				.setBilledNumber (
					"bill")

				.setBilledMessage (
					textHelper.findOrCreate (
						transaction,
						"Billed message"))

				.setFreeRouter (
					routerHelper.findByCodeRequired (
						transaction,
						routeHelper.findByCodeRequired (
							transaction,
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
						transaction,
						"Subsription confirmed"))

				.setUnsubscribeMessageText (
					textHelper.findOrCreate (
						transaction,
						"Subscription cancelled"))

			);

			transaction.flush ();

			KeywordSetRec inboundKeywordSet =
				keywordSetHelper.findByCodeRequired (
					transaction,
					GlobalId.root,
					"test",
					"inbound");

			keywordHelper.insert (
				transaction,
				keywordHelper.createInstance ()

				.setKeywordSet (
					inboundKeywordSet)

				.setKeyword (
					"sub")

				.setDescription (
					"Subscription subscribe")

				.setCommand (
					commandHelper.findByCodeRequired (
						transaction,
						subscription,
						"subscribe"))

			);

			keywordHelper.insert (
				transaction,
				keywordHelper.createInstance ()

				.setKeywordSet (
					inboundKeywordSet)

				.setKeyword (
					"unsub")

				.setDescription (
					"Subscription unsubscribe")

				.setCommand (
					commandHelper.findByCodeRequired (
						transaction,
						subscription,
						"unsubscribe"))

			);

			for (
				Map.Entry<String,String> listSpecEntry
					: listSpecs.entrySet ()
			) {

				SubscriptionListRec list =
					subscriptionListHelper.insert (
						transaction,
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
					transaction,
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
						transaction,
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

				transaction.flush ();

				keywordHelper.insert (
					transaction,
					keywordHelper.createInstance ()

					.setKeywordSet (
						inboundKeywordSet)

					.setKeyword (
						affiliateSpecEntry.getKey ())

					.setDescription (
						affiliateSpecEntry.getValue ())

					.setCommand (
						commandHelper.findByCodeRequired (
							transaction,
							affiliate,
							"subscribe"))

				);

			}

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