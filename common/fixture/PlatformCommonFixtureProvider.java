package wbs.platform.common.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuGroupRec;

@PrototypeComponent ("platformCommonFixtureProvider")
public
class PlatformCommonFixtureProvider
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
				"internal")

			.setLabel (
				"Internals")

			.setOrder (
				70)

		);

		menuGroupHelper.insert (
			new MenuGroupRec ()

			.setCode (
				"system")

			.setLabel (
				"System")

			.setOrder (
				50));

	}

}
