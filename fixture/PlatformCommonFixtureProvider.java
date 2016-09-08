package wbs.platform.common.fixture;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuGroupRec;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;

@PrototypeComponent ("platformCommonFixtureProvider")
public
class PlatformCommonFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

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

		menuGroupHelper.insert (
			menuGroupHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCodeRequired (
					GlobalId.root,
					"test"))

			.setCode (
				"internal")

			.setName (
				"Internal")

			.setDescription (
				"")

			.setLabel (
				"Internals")

			.setOrder (
				70l)

		);

		MenuGroupRec systemMenuGroup =
			menuGroupHelper.insert (
				menuGroupHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCodeRequired (
					GlobalId.root,
					"test"))

			.setCode (
				"system")

			.setName (
				"System")

			.setDescription (
				"")

			.setLabel (
				"System")

			.setOrder (
				50l)

		);

		menuItemHelper.insert (
			menuItemHelper.createInstance ()

			.setMenuGroup (
				systemMenuGroup)

			.setCode (
				"slice")

			.setName (
				"Slice")

			.setDescription (
				"")

			.setLabel (
				"Slice")

			.setTargetPath (
				"/slices")

			.setTargetFrame (
				"main")

		);

		menuItemHelper.insert (
			menuItemHelper.createInstance ()

			.setMenuGroup (
				systemMenuGroup)

			.setCode (
				"menu")

			.setName (
				"Menu")

			.setDescription (
				"")

			.setLabel (
				"Menu")

			.setTargetPath (
				"/menuGroups")

			.setTargetFrame (
				"main")

		);

	}

}
