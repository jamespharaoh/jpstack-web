package wbs.platform.scaffold.fixture;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Transaction;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.scaffold.model.SliceObjectHelper;

@PrototypeComponent ("sliceFixtureProvider")
public
class SliceFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	// implementation

	@Override
	public
	void createFixtures (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Transaction transaction) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createFixtures");

		sliceHelper.insert (
			taskLogger,
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
