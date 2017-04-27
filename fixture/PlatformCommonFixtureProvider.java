package wbs.platform.common.fixture;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;

@PrototypeComponent ("platformCommonFixtureProvider")
public
class PlatformCommonFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

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
			@NonNull OwnedTransaction transaction) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createFixtures");

		) {

			createMenuGroups (
				taskLogger,
				transaction);

			createMenuItems (
				taskLogger,
				transaction);

		}

	}

	private
	void createMenuGroups (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull OwnedTransaction transaction) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createMenuGroups");

		) {

			menuGroupHelper.insert (
				taskLogger,
				menuGroupHelper.createInstance ()

				.setSlice (
					sliceHelper.findByCodeRequired (
						GlobalId.root,
						"test"))

				.setCode (
					"panel")

				.setName (
					"Panel")

				.setDescription (
					"")

				.setLabel (
					"Panels")

				.setOrder (
					20l)

			);

			menuGroupHelper.insert (
				taskLogger,
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

			menuGroupHelper.insert (
				taskLogger,
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

			transaction.flush ();

		}

	}

	private
	void createMenuItems (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull OwnedTransaction transaction) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createMenuItems");

		) {

			menuItemHelper.insert (
				taskLogger,
				menuItemHelper.createInstance ()

				.setMenuGroup (
					menuGroupHelper.findByCodeRequired (
						GlobalId.root,
						"test",
						"system"))

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
				taskLogger,
				menuItemHelper.createInstance ()

				.setMenuGroup (
					menuGroupHelper.findByCodeRequired (
						GlobalId.root,
						"test",
						"system"))

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

			transaction.flush ();

		}

	}

}
