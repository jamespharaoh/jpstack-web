package wbs.smsapps.autoresponder.fixture;

import lombok.NonNull;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.TaskLogger;

import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;

@PrototypeComponent ("autoResponderFixtureProvider")
public
class AutoResponderFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	// implementation

	@Override
	public
	void createFixtures (
			@NonNull TaskLogger parentTaskLogger) {

		menuItemHelper.insert (
			menuItemHelper.createInstance ()

			.setMenuGroup (
				menuGroupHelper.findByCodeRequired (
					GlobalId.root,
					"test",
					"facility"))

			.setCode (
				"auto_responder")

			.setName (
				"Auto responder")

			.setDescription (
				"")

			.setLabel (
				"Auto responder")

			.setTargetPath (
				"/autoResponders")

			.setTargetFrame (
				"main"));

	}

}
