package wbs.platform.group.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.group.model.GroupObjectHelper;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;

@PrototypeComponent ("groupFixtureProvider")
public
class GroupFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	GroupObjectHelper groupHelper;

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

		// menu item

		menuItemHelper.insert (
			menuItemHelper.createInstance ()

			.setMenuGroup (
				menuGroupHelper.findByCodeRequired (
					GlobalId.root,
					"test",
					"system"))

			.setCode (
				"group")

			.setName (
				"Group")

			.setDescription (
				"Group")

			.setLabel (
				"Groups")

			.setTargetPath (
				"/groups")

			.setTargetFrame (
				"main")

		);

		// test groups

		for (
			int index = 0;
			index < 10;
			index ++
		) {

			groupHelper.insert (
				groupHelper.createInstance ()

				.setSlice (
					sliceHelper.findByCodeRequired (
						GlobalId.root,
						"test"))

				.setCode (
					"test_" + index)

				.setName (
					"Test " + index)

				.setDescription (
					"")

			);

		}

	}

}
