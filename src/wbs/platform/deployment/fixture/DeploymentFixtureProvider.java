package wbs.platform.deployment.fixture;

import static wbs.utils.etc.NetworkUtils.runHostname;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

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
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createFixtures");

		createMenuItems (
			taskLogger);

		createConsoleDeployments (
			taskLogger);

	}

	private
	void createMenuItems (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createMenuItems");

		menuItemHelper.insert (
			taskLogger,
			menuItemHelper.createInstance ()

			.setMenuGroup (
				menuGroupHelper.findByCodeRequired (
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

	private
	void createConsoleDeployments (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createConsoleDeployments");

		consoleDeploymentHelper.insert (
			taskLogger,
			consoleDeploymentHelper.createInstance ()

			.setCode (
				"console_dev")

			.setName (
				"Console dev")

			.setDescription (
				"Console dev")

			.setHost (
				runHostname ())

		);

	}

}
