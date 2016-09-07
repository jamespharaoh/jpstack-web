package wbs.platform.scaffold.fixture;

import javax.inject.Inject;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.platform.scaffold.model.SliceObjectHelper;

@PrototypeComponent ("sliceFixtureProvider")
public
class SliceFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	SliceObjectHelper sliceHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		sliceHelper.insert (
			sliceHelper.createInstance ()

			.setCode (
				"test")

			.setName (
				"Test")

			.setDescription (
				"Test")

		);

	}

}
