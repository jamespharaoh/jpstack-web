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
import wbs.smsapps.manualresponder.model.ManualResponderObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderRec;

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
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	MenuItemObjectHelper menuItemHelper;

	@Inject
	SliceObjectHelper sliceHelper;

	// implementation

	@Override
	public
	void createFixtures () {

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

		menuItemHelper.insert (
			new MenuItemRec ()

			.setMenuGroup (
				menuGroupHelper.findByCode (
					GlobalId.root,
					"test",
					"facility"))

			.setCode (
				"chat")

			.setName (
				"Chat")

			.setDescription (
				"APN SMS chat service")

			.setLabel (
				"Chat")

			.setTargetPath (
				"/chats")

			.setTargetFrame (
				"main")

		);

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

	}

}
