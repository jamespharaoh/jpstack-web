package wbs.smsapps.forwarder.fixture;

import javax.inject.Inject;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;

@PrototypeComponent ("forwarderFixtureProvider")
public
class ForwarderFixtureProvider
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
				menuGroupHelper.findByCodeRequired (
					GlobalId.root,
					"test",
					"facility"))

			.setCode (
				"forwarder")

			.setName (
				"Forwarder")

			.setDescription (
				"")

			.setLabel (
				"Forwarder")

			.setTargetPath (
				"/forwarders")

			.setTargetFrame (
				"main")

		);

	}

}
