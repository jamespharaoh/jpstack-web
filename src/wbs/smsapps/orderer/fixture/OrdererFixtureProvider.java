package wbs.smsapps.orderer.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;

@PrototypeComponent ("ordererFixtureProvider")
public
class OrdererFixtureProvider
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
				menuGroupHelper.findByCode (
					GlobalId.root,
					"test",
					"facility"))

			.setCode (
				"orderer")

			.setName (
				"Orderer")

			.setDescription (
				"Place order by SMS service")

			.setLabel (
				"Orderer")

			.setTargetPath (
				"/orderers")

			.setTargetFrame (
				"main")

		);

	}

}
