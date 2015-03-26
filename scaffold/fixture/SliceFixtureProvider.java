package wbs.platform.scaffold.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.scaffold.model.SliceRec;

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
			new SliceRec ()

			.setCode (
				"test")

			.setName (
				"Test")

			.setDescription (
				"Test")

		);

	}

}
