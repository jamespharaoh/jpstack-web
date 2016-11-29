package wbs.platform.feature.fixture;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.TaskLogger;

import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;

public
class FeatureFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	// public implementation

	@Override
	public
	void createFixtures (
			@NonNull TaskLogger parentTaskLogger) {

		createMenuItems ();

	}

	// private implementation

	private
	void createMenuItems () {

		menuItemHelper.insert (
			menuItemHelper.createInstance ()

			.setMenuGroup (
				menuGroupHelper.findByCodeRequired (
					GlobalId.root,
					"test",
					"internal"))

			.setCode (
				"feature")

			.setName (
				"Feature")

			.setDescription (
				"")

			.setLabel (
				"Features")

			.setTargetPath (
				"/features")

			.setTargetFrame (
				"main")

		);

	}

}
