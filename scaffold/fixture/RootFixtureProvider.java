package wbs.platform.scaffold.fixture;

import javax.inject.Inject;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.utils.RandomLogic;
import wbs.platform.scaffold.model.RootObjectHelper;

@PrototypeComponent ("rootFixtureProvider")
public
class RootFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	RandomLogic randomLogic;

	@Inject
	RootObjectHelper rootHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		rootHelper.insert (
			rootHelper.createInstance ()

			.setId (
				0l)

			.setCode (
				"root")

			.setFixturesSeed (
				randomLogic.generateLowercase (
					20))

		);

	}

}
