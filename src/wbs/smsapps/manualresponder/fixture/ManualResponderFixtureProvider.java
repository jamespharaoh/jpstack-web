package wbs.smsapps.manualresponder.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuGroupRec;
import wbs.platform.menu.model.MenuObjectHelper;
import wbs.platform.menu.model.MenuRec;

@PrototypeComponent ("manualResponderFixtureProvider")
public
class ManualResponderFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	MenuObjectHelper menuHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		MenuGroupRec facilityMenuGroup =
			menuGroupHelper.findByCode (
				GlobalId.root,
				"facility");

		menuHelper.insert (
			new MenuRec ()

			.setMenuGroup (
				facilityMenuGroup)

			.setCode (
				"manual_responder")

			.setLabel (
				"Manual responder")

			.setPath (
				"/manualResponders"));

	}


}
