package wbs.platform.scaffold.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.platform.scaffold.model.RootObjectHelper;

@PrototypeComponent ("rootFixtureProvider")
public
class RootFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	RootObjectHelper rootHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		rootHelper.insert (
			rootHelper.createInstance ()

			.setId (
				0)

			.setCode (
				"root")

		);

	}

}
