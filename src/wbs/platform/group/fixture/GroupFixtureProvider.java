package wbs.platform.group.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuObjectHelper;
import wbs.platform.menu.model.MenuRec;

@PrototypeComponent ("groupFixtureProvider")
public
class GroupFixtureProvider
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
		// TODO Auto-generated method stub

		menuHelper.insert (
			new MenuRec ()

			.setMenuGroup (
				menuGroupHelper.findByCode (
					GlobalId.root,
					"system"))

			.setCode (
				"group")

			.setLabel (
				"Groups")

			.setPath (
				"/groups")

		);

	}

}
