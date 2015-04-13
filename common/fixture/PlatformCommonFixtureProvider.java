package wbs.platform.common.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuGroupRec;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.menu.model.MenuItemRec;
import wbs.platform.scaffold.model.SliceObjectHelper;

@PrototypeComponent ("platformCommonFixtureProvider")
public
class PlatformCommonFixtureProvider
	implements FixtureProvider {

	// dependencies

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

		menuGroupHelper.insert (
			new MenuGroupRec ()

			.setSlice (
				sliceHelper.findByCode (
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
				70)

		);

		MenuGroupRec systemMenuGroup =
			menuGroupHelper.insert (
				new MenuGroupRec ()

			.setSlice (
				sliceHelper.findByCode (
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
				50));

		menuItemHelper.insert (
			new MenuItemRec ()

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
			new MenuItemRec ()

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
