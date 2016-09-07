package wbs.integrations.oxygen8.fixture;

import javax.inject.Inject;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;

@PrototypeComponent ("oxygen8FixtureProvider")
public
class Oxygen8FixtureProvider
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
					"integration"))

			.setCode (
				"oxygen8")

			.setName (
				"Oxygen8")

			.setDescription (
				"")

			.setLabel (
				"Oxygen8")

			.setTargetPath (
				"/oxygen8")

			.setTargetFrame (
				"main")

		);

	}

}
