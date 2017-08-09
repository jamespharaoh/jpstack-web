package wbs.framework.fixtures;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.GenericConfigLoader;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("testAccounts")
public
class FixturesTestAccounts
	implements ComponentFactory <TestAccounts> {

	// singleton dependencies

	@SingletonDependency
	GenericConfigLoader genericConfigLoader;

	@ClassSingletonDependency
	LogContext logContext;

	// components

	@Override
	public
	TestAccounts makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"testAccounts");

		) {

			return (TestAccounts) (
				new TestAccounts ()

				.genericConfigSpec (
					genericConfigLoader.loadSpec (
						taskLogger,
						"config/test-accounts.xml"))

			);

		}

	}

}
