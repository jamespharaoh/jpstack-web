package wbs.smsapps.broadcast.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;

@PrototypeComponent ("broadcastFixtureProvider")
public
class BroadcastFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	MenuItemObjectHelper menuItemHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		menuItemHelper.insert (
			menuItemHelper.createInstance ()

			.setMenuGroup (
				menuGroupHelper.findByCodeOrNull (
					GlobalId.root,
					"test",
					"facility"))

			.setCode (
				"broadcast")

			.setName (
				"Broadcast")

			.setDescription (
				"")

			.setLabel (
				"Broadcast")

			.setTargetPath (
				"/broadcastConfigs")

			.setTargetFrame (
				"main")

		);

	}

}
