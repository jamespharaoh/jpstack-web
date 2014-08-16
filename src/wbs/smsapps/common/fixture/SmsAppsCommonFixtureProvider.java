package wbs.smsapps.common.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuGroupRec;

@PrototypeComponent ("smsAppsCommonFixtureProvider")
public
class SmsAppsCommonFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		menuGroupHelper.insert (
			new MenuGroupRec ()

			.setCode (
				"facility")

			.setLabel (
				"Facilities")

			.setOrder (
				20)

		);

	}

}
