package wbs.framework.fixtures;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.GenericConfigLoader;
import wbs.framework.component.config.WbsSpecialConfig;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.utils.thread.ThreadManager;
import wbs.utils.thread.ThreadManagerImplementation;

@SingletonComponent ("fixturesComponents")
public
class FixturesComponents {

	// singleton dependencies

	@SingletonDependency
	GenericConfigLoader genericConfigLoader;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype components

	@PrototypeDependency
	Provider <ThreadManagerImplementation> threadManagerImplemetationProvider;

	// components

	@SingletonComponent ("testAccounts")
	public
	TestAccounts testAccounts (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"testAccounts");

		return (TestAccounts) (
			new TestAccounts ()

			.genericConfigSpec (
				genericConfigLoader.loadSpec (
					taskLogger,
					"config/test-accounts.xml"))

		);

	}

	@SingletonComponent ("threadManager")
	public
	ThreadManager threadManager () {

		return threadManagerImplemetationProvider.get ();

	}

	@SingletonComponent ("wbsSpecialConfig")
	public
	WbsSpecialConfig wbsSpecialConfig () {

		return new WbsSpecialConfig ()

			.assumeNegativeCache (
				true);

	}

}
