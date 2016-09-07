package wbs.integrations.mediaburst.fixture;

import javax.inject.Inject;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;

@PrototypeComponent ("mediaburstFixtureProvider")
public
class MediaburstFixtureProvider
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
				"mediaburst")

			.setName (
				"Mediaburst")

			.setDescription (
				"")

			.setLabel (
				"Mediaburst")

			.setTargetPath (
				"/mediaburst")

			.setTargetFrame (
				"main")

		);

	}

}
