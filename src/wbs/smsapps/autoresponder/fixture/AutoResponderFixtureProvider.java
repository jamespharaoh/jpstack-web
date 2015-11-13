package wbs.smsapps.autoresponder.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;

@PrototypeComponent ("autoResponderFixtureProvider")
public
class AutoResponderFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	MenuItemObjectHelper menuItemHelper;

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		menuItemHelper.insert (
			menuItemHelper.createInstance ()

			.setMenuGroup (
				menuGroupHelper.findByCode (
					GlobalId.root,
					"test",
					"facility"))

			.setCode (
				"auto_responder")

			.setName (
				"Auto responder")

			.setDescription (
				"")

			.setLabel (
				"Auto responder")

			.setTargetPath (
				"/autoResponders")

			.setTargetFrame (
				"main"));

	}

}
