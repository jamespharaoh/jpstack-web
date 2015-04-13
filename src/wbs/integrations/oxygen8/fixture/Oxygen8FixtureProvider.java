package wbs.integrations.oxygen8.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.menu.model.MenuItemRec;

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
			new MenuItemRec ()

			.setMenuGroup (
				menuGroupHelper.findByCode (
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
