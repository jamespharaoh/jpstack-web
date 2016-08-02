package wbs.integrations.clockworksms.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;

@PrototypeComponent ("clockworkSmsFixtureProvider")
public
class ClockworkSmsFixtureProvider
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
				"clockwork_sms")

			.setName (
				"Clockwork SMS")

			.setDescription (
				"")

			.setLabel (
				"Clockwork SMS")

			.setTargetPath (
				"/clockworkSms")

			.setTargetFrame (
				"main")

		);

	}

}
