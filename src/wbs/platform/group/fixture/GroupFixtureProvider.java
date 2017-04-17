package wbs.platform.group.fixture;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

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

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	// implementation

	@Override
	public
	void createFixtures (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Transaction transaction) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createFixtures");

		// menu item

		menuItemHelper.insert (
			taskLogger,
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
				taskLogger,
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
