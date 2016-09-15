package wbs.platform.scaffold.fixture;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.fixtures.FixtureProvider;
import wbs.platform.scaffold.model.RootObjectHelper;
import wbs.utils.random.RandomLogic;

@PrototypeComponent ("rootFixtureProvider")
public
class RootFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	RandomLogic randomLogic;

	@SingletonDependency
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
