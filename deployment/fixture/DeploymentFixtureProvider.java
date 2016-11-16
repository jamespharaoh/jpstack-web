package wbs.platform.deployment.fixture;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;

import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;

@PrototypeComponent ("deploymentFixtureProvider")
public
class DeploymentFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		menuItemHelper.insert (
			menuItemHelper.createInstance ()

			.setMenuGroup (
				menuGroupHelper.findByCodeRequired (
					GlobalId.root,
					"test",
					"internal"))

			.setCode (
				"console_deployment")

			.setName (
				"Console deployment")

			.setDescription (
				"")

			.setLabel (
				"Console deployments")

			.setTargetPath (
				"/consoleDeployments")

			.setTargetFrame (
				"main")

		);

	}

}
