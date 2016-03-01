package wbs.smsapps.manualresponder.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
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
import wbs.smsapps.manualresponder.model.ManualResponderObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderRec;
import wbs.smsapps.manualresponder.model.ManualResponderTemplateObjectHelper;

@PrototypeComponent ("manualResponderFixtureProvider")
public
class ManualResponderFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	CurrencyObjectHelper currencyHelper;

	@Inject
	KeywordObjectHelper keywordHelper;

	@Inject
	KeywordSetObjectHelper keywordSetHelper;

	@Inject
	ManualResponderObjectHelper manualResponderHelper;

	@Inject
	ManualResponderTemplateObjectHelper manualResponderTemplateHelper;

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
	SmsCustomerManagerObjectHelper smsCustomerManagerHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		createMenuItem ();

		createManualResponder ();

	}

	private
	void createMenuItem () {

		menuItemHelper.insert (
			menuItemHelper.createInstance ()

			.setMenuGroup (
				menuGroupHelper.findByCode (
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

	}

	private
	void createManualResponder () {

		ManualResponderRec manualResponder =
			manualResponderHelper.insert (
				manualResponderHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCode (
					GlobalId.root,
					"test"))

			.setCode (
				"test")

			.setName (
				"Test")

			.setDescription (
				"Test manual responder")

			.setCurrency (
				currencyHelper.findByCode (
					GlobalId.root,
					"test",
					"gbp"))

			.setSmsCustomerManager (
				smsCustomerManagerHelper.findByCode (
					GlobalId.root,
					"test",
					"customer_manager"))

			.setRequiredAge (
				18l)

		);

		keywordHelper.insert (
			keywordHelper.createInstance ()

			.setKeywordSet (
				keywordSetHelper.findByCode (
					GlobalId.root,
					"test",
					"inbound"))

			.setKeyword (
				"mr")

			.setDescription (
				"Test manual responder")

			.setCommand (
				commandHelper.findByCode (
					manualResponder,
					"default"))

		);

		createManualResponderTemplates (
			manualResponder);

	}

	private
	void createManualResponderTemplates (
			ManualResponderRec manualResponder) {

		manualResponderTemplateHelper.insert (
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
				routerHelper.findByCode (
					routeHelper.findByCode (
						GlobalId.root,
						"test",
						"free"),
					"static"))

		);

		manualResponderTemplateHelper.insert (
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
				routerHelper.findByCode (
					routeHelper.findByCode (
						GlobalId.root,
						"test",
						"bill"),
					"static"))

		);

		manualResponderTemplateHelper.insert (
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
				routerHelper.findByCode (
					routeHelper.findByCode (
						GlobalId.root,
						"test",
						"bill"),
					"static"))

		);

		manualResponderTemplateHelper.insert (
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
				routerHelper.findByCode (
					routeHelper.findByCode (
						GlobalId.root,
						"test",
						"bill"),
					"static"))

		);

		manualResponderTemplateHelper.insert (
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
				routerHelper.findByCode (
					routeHelper.findByCode (
						GlobalId.root,
						"test",
						"free"),
					"static"))

		);

		manualResponder.setDateOfBirthTemplate (
			manualResponderTemplateHelper.insert (
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
				routerHelper.findByCode (
					routeHelper.findByCode (
						GlobalId.root,
						"test",
						"free"),
					"static"))

			.setReplyKeywordSet (
				keywordSetHelper.findByCode (
					GlobalId.root,
					"test",
					"inbound"))

		));

		manualResponder.setDateOfBirthErrorTemplate (
			manualResponderTemplateHelper.insert (
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
				routerHelper.findByCode (
					routeHelper.findByCode (
						GlobalId.root,
						"test",
						"free"),
					"static"))

			.setReplyKeywordSet (
				keywordSetHelper.findByCode (
					GlobalId.root,
					"test",
					"inbound"))

		));

		manualResponder.setTooYoungTemplate (
			manualResponderTemplateHelper.insert (
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
				routerHelper.findByCode (
					routeHelper.findByCode (
						GlobalId.root,
						"test",
						"free"),
					"static"))

			.setReplyKeywordSet (
				keywordSetHelper.findByCode (
					GlobalId.root,
					"test",
					"inbound"))

		));

	}

}
