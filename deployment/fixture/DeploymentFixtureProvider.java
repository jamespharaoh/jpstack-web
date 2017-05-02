package wbs.platform.deployment.fixture;

import static wbs.utils.etc.NetworkUtils.runHostname;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;

import wbs.platform.deployment.model.ConsoleDeploymentObjectHelper;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;

@PrototypeComponent ("deploymentFixtureProvider")
public
class DeploymentFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	ConsoleDeploymentObjectHelper consoleDeploymentHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	// implementation

	@Override
	public
	void createFixtures (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createFixtures");

		) {

			createMenuItems (
				transaction);

			createConsoleDeployments (
				transaction);

		}

	}

	private
	void createMenuItems (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createMenuItems");

		) {

			menuItemHelper.insert (
				transaction,
				menuItemHelper.createInstance ()

				.setMenuGroup (
					menuGroupHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						"test",
						"internal"))

				.setCode (
					"deployment")

				.setName (
					"Deployment")

				.setDescription (
					"")

				.setLabel (
					"Deployments")

				.setTargetPath (
					"/deployment")

				.setTargetFrame (
					"main")

			);

		}

	}

	private
	void createConsoleDeployments (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createConsoleDeployments");

		) {

			consoleDeploymentHelper.insert (
				transaction,
				consoleDeploymentHelper.createInstance ()

				.setCode (
					"test")

				.setName (
					"Test")

				.setDescription (
					"Console test deployment")

				.setHost (
					runHostname ())

			);

		}

	}

}
