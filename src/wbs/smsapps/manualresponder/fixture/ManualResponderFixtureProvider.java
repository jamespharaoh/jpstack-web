package wbs.smsapps.manualresponder.fixture;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;

import wbs.platform.currency.model.CurrencyObjectHelper;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;

import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.customer.model.SmsCustomerManagerObjectHelper;
import wbs.sms.keyword.model.KeywordObjectHelper;
import wbs.sms.keyword.model.KeywordSetObjectHelper;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.router.model.RouterObjectHelper;
import wbs.sms.spendlimit.model.SmsSpendLimiterObjectHelper;

import wbs.smsapps.manualresponder.model.ManualResponderObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderRec;
import wbs.smsapps.manualresponder.model.ManualResponderTemplateObjectHelper;

@PrototypeComponent ("manualResponderFixtureProvider")
public
class ManualResponderFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@SingletonDependency
	CurrencyObjectHelper currencyHelper;

	@SingletonDependency
	KeywordObjectHelper keywordHelper;

	@SingletonDependency
	KeywordSetObjectHelper keywordSetHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ManualResponderObjectHelper manualResponderHelper;

	@SingletonDependency
	ManualResponderTemplateObjectHelper manualResponderTemplateHelper;

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
	SmsCustomerManagerObjectHelper smsCustomerManagerHelper;

	@SingletonDependency
	SmsSpendLimiterObjectHelper smsSpendLimiterHelper;

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

			createMenuItem (
				transaction);

			createManualResponder (
				transaction);

		}

	}

	private
	void createMenuItem (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createMenuItem");

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
					"manual_responder")

				.setName (
					"Manual responder")

				.setDescription (
					"Manual responder services")

				.setLabel (
					"Manual responder")

				.setTargetPath (
					"/manualResponders")

				.setTargetFrame (
					"main")

			);

			transaction.flush ();

		}

	}

	private
	void createManualResponder (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createManualResponder");

		) {

			ManualResponderRec manualResponder =
				manualResponderHelper.insert (
					transaction,
					manualResponderHelper.createInstance ()

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
					"Test manual responder")

				.setCurrency (
					currencyHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						"test",
						"gbp"))

				.setSmsSpendLimiter (
					smsSpendLimiterHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						"test",
						"test_sms_spend_limiter"))

				.setSmsCustomerManager (
					smsCustomerManagerHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						"test",
						"customer_manager"))

				.setRequiredAge (
					18l)

			);

			transaction.flush ();

			keywordHelper.insert (
				transaction,
				keywordHelper.createInstance ()

				.setKeywordSet (
					keywordSetHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						"test",
						"inbound"))

				.setKeyword (
					"mr")

				.setDescription (
					"Test manual responder")

				.setCommand (
					commandHelper.findByCodeRequired (
						transaction,
						manualResponder,
						"default"))

			);

			createManualResponderTemplates (
				transaction,
				manualResponder);

			transaction.flush ();

		}

	}

	private
	void createManualResponderTemplates (
			@NonNull Transaction parentTransaction,
			@NonNull ManualResponderRec manualResponder) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createManualResponderTemplates");

		) {

			manualResponderTemplateHelper.insert (
				transaction,
				manualResponderTemplateHelper.createInstance ()

				.setManualResponder (
					manualResponder)

				.setCode (
					"fixed")

				.setName (
					"Fixed")

				.setDescription (
					"Fixed")

				.setCustomisable (
					false)

				.setDefaultText (
					"This is a fixed message")

				.setMaximumMessages (
					1l)

				.setMinimumMessageParts (
					1l)

				.setNumber (
					"free")

				.setRouter (
					routerHelper.findByCodeRequired (
						transaction,
						routeHelper.findByCodeRequired (
							transaction,
							GlobalId.root,
							"test",
							"free"),
						"static"))

			);

			manualResponderTemplateHelper.insert (
				transaction,
				manualResponderTemplateHelper.createInstance ()

				.setManualResponder (
					manualResponder)

				.setCode (
					"short_billed")

				.setName (
					"Short Billed")

				.setDescription (
					"Short Billed")

				.setCustomisable (
					true)

				.setApplyTemplates (
					true)

				.setSplitLong (
					false)

				.setSingleTemplate (
					"BILLED MESSAGE: {message}")

				.setFirstTemplate (
					"BILLED MESSAGE {page}/{pages}: {message}")

				.setMiddleTemplate (
					"BILLED MESSAGE {page}/{pages}: {message}")

				.setLastTemplate (
					"BILLED MESSAGE {page}/{pages}: {message}")

				.setMaximumMessages (
					1l)

				.setMinimumMessageParts (
					1l)

				.setNumber (
					"bill")

				.setRouter (
					routerHelper.findByCodeRequired (
						transaction,
						routeHelper.findByCodeRequired (
							transaction,
							GlobalId.root,
							"test",
							"bill"),
						"static"))

			);

			manualResponderTemplateHelper.insert (
				transaction,
				manualResponderTemplateHelper.createInstance ()

				.setManualResponder (
					manualResponder)

				.setCode (
					"long_billed_join")

				.setName (
					"Long Billed Join")

				.setDescription (
					"Long Billed Join")

				.setCustomisable (
					true)

				.setApplyTemplates (
					true)

				.setSingleTemplate (
					"BILLED MESSAGE: {message}")

				.setFirstTemplate (
					"BILLED MESSAGE {page}/{pages}: {message}")

				.setMiddleTemplate (
					"BILLED MESSAGE {page}/{pages}: {message}")

				.setLastTemplate (
					"BILLED MESSAGE {page}/{pages}: {message}")

				.setSplitLong (
					false)

				.setMaximumMessages (
					3l)

				.setMinimumMessageParts (
					2l)

				.setNumber (
					"bill")

				.setRouter (
					routerHelper.findByCodeRequired (
						transaction,
						routeHelper.findByCodeRequired (
							transaction,
							GlobalId.root,
							"test",
							"bill"),
						"static"))

			);

			manualResponderTemplateHelper.insert (
				transaction,
				manualResponderTemplateHelper.createInstance ()

				.setManualResponder (
					manualResponder)

				.setCode (
					"long_billed_split")

				.setName (
					"Long Billed Split")

				.setDescription (
					"Long Billed Split")

				.setCustomisable (
					true)

				.setApplyTemplates (
					true)

				.setSingleTemplate (
					"BILLED MESSAGE: {message}")

				.setFirstTemplate (
					"BILLED MESSAGE {page}/{pages}: {message}")

				.setMiddleTemplate (
					"BILLED MESSAGE {page}/{pages}: {message}")

				.setLastTemplate (
					"BILLED MESSAGE {page}/{pages}: {message}")

				.setSplitLong (
					true)

				.setMaximumMessages (
					3l)

				.setMinimumMessageParts (
					2l)

				.setNumber (
					"bill")

				.setRouter (
					routerHelper.findByCodeRequired (
						transaction,
						routeHelper.findByCodeRequired (
							transaction,
							GlobalId.root,
							"test",
							"bill"),
						"static"))

			);

			manualResponderTemplateHelper.insert (
				transaction,
				manualResponderTemplateHelper.createInstance ()

				.setManualResponder (
					manualResponder)

				.setCode (
					"free")

				.setName (
					"Free")

				.setDescription (
					"Free")

				.setCustomisable (
					true)

				.setSingleTemplate (
					"")

				.setMaximumMessages (
					1l)

				.setMinimumMessageParts (
					1l)

				.setNumber (
					"inbound")

				.setRouter (
					routerHelper.findByCodeRequired (
						transaction,
						routeHelper.findByCodeRequired (
							transaction,
							GlobalId.root,
							"test",
							"free"),
						"static"))

			);

			manualResponder.setDateOfBirthTemplate (
				manualResponderTemplateHelper.insert (
					transaction,
					manualResponderTemplateHelper.createInstance ()

				.setManualResponder (
					manualResponder)

				.setCode (
					"welcome")

				.setName (
					"Welcome")

				.setDescription (
					"Welcome")

				.setCustomisable (
					false)

				.setDefaultText (
					"Welcome, please provide with name and date of birth")

				.setMaximumMessages (
					1l)

				.setMinimumMessageParts (
					1l)

				.setNumber (
					"inbound")

				.setRouter (
					routerHelper.findByCodeRequired (
						transaction,
						routeHelper.findByCodeRequired (
							transaction,
							GlobalId.root,
							"test",
							"free"),
						"static"))

				.setReplyKeywordSet (
					keywordSetHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						"test",
						"inbound"))

			));

			manualResponder.setDateOfBirthErrorTemplate (
				manualResponderTemplateHelper.insert (
					transaction,
					manualResponderTemplateHelper.createInstance ()

				.setManualResponder (
					manualResponder)

				.setCode (
					"date_of_birth_error")

				.setName (
					"Date of birth error")

				.setDescription (
					"Date of birth error")

				.setCustomisable (
					false)

				.setHidden (
					true)

				.setDefaultText (
					"Your message was not understood, please try again")

				.setMaximumMessages (
					1l)

				.setMinimumMessageParts (
					1l)

				.setNumber (
					"inbound")

				.setRouter (
					routerHelper.findByCodeRequired (
						transaction,
						routeHelper.findByCodeRequired (
							transaction,
							GlobalId.root,
							"test",
							"free"),
						"static"))

				.setReplyKeywordSet (
					keywordSetHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						"test",
						"inbound"))

			));

			manualResponder.setTooYoungTemplate (
				manualResponderTemplateHelper.insert (
					transaction,
					manualResponderTemplateHelper.createInstance ()

				.setManualResponder (
					manualResponder)

				.setCode (
					"too_young")

				.setName (
					"Too young")

				.setDescription (
					"Too young")

				.setCustomisable (
					false)

				.setHidden (
					true)

				.setDefaultText (
					"Sorry, you are too young to use this service")

				.setMaximumMessages (
					1l)

				.setMinimumMessageParts (
					1l)

				.setNumber (
					"inbound")

				.setRouter (
					routerHelper.findByCodeRequired (
						transaction,
						routeHelper.findByCodeRequired (
							transaction,
							GlobalId.root,
							"test",
							"free"),
						"static"))

				.setReplyKeywordSet (
					keywordSetHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						"test",
						"inbound"))

			));

		}

	}

}
