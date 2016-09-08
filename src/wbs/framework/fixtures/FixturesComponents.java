package wbs.framework.fixtures;

import javax.inject.Provider;

import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.GenericConfigLoader;
import wbs.framework.utils.ThreadManager;
import wbs.framework.utils.ThreadManagerImplementation;

@SingletonComponent ("fixturesComponents")
public
class FixturesComponents {

	// singleton dependencies

	@SingletonDependency
	GenericConfigLoader genericConfigLoader;

	// prototype components

	@PrototypeDependency
	Provider <ThreadManagerImplementation> threadManagerImplemetationProvider;

	// components

	@SingletonComponent ("testAccounts")
	public
	TestAccounts testAccounts () {

		return (TestAccounts) (
			new TestAccounts ()

			.genericConfigSpec (
				genericConfigLoader.loadSpec (
					"config/test-accounts.xml"))

		);

	}

	@SingletonComponent ("threadManager")
	public
	ThreadManager threadManager () {

		return threadManagerImplemetationProvider.get ();

	}

}
