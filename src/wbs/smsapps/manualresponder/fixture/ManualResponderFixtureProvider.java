package wbs.smsapps.manualresponder.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.currency.model.CurrencyObjectHelper;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.menu.model.MenuItemRec;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.keyword.model.KeywordObjectHelper;
import wbs.sms.keyword.model.KeywordRec;
import wbs.sms.keyword.model.KeywordSetObjectHelper;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.router.model.RouterObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderRec;
import wbs.smsapps.manualresponder.model.ManualResponderTemplateObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderTemplateRec;

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
			new MenuItemRec ()

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
				new ManualResponderRec ()

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
					"gbp"))

		);

		keywordHelper.insert (
			new KeywordRec ()

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
			new ManualResponderTemplateRec ()

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
				1)

			.setMinimumMessageParts (
				1)

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
			new ManualResponderTemplateRec ()

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
				1)

			.setMinimumMessageParts (
				1)

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
			new ManualResponderTemplateRec ()

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
				3)

			.setMinimumMessageParts (
				2)

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
			new ManualResponderTemplateRec ()

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
				3)

			.setMinimumMessageParts (
				2)

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
			new ManualResponderTemplateRec ()

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
				1)

			.setMinimumMessageParts (
				1)

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

	}

}
