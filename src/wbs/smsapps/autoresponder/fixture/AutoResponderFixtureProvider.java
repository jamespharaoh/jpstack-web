package wbs.smsapps.autoresponder.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuObjectHelper;
import wbs.platform.menu.model.MenuRec;

@PrototypeComponent ("autoResponderFixtureProvider")
public
class AutoResponderFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	MenuObjectHelper menuHelper;

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		menuHelper.insert (
			new MenuRec ()

			.setMenuGroup (
				menuGroupHelper.findByCode (
					GlobalId.root,
					"facility"))

			.setCode (
				"auto_responder")

			.setLabel (
				"Auto responder")

			.setPath (
				"/autoResponders"));

	}

}
