package wbs.platform.group.fixture;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.platform.group.model.GroupObjectHelper;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;

@PrototypeComponent ("groupFixtureProvider")
public
class GroupFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	GroupObjectHelper groupHelper;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
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
