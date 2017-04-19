package wbs.platform.deployment.logic;

import wbs.platform.deployment.model.ApiDeploymentRec;
import wbs.platform.deployment.model.ConsoleDeploymentRec;
import wbs.platform.deployment.model.DaemonDeploymentRec;

public
interface DeploymentLogic {

	//AgentDeploymentRec thisAgentDeployment ();

	ApiDeploymentRec thisApiDeployment ();

	ConsoleDeploymentRec thisConsoleDeployment ();

	DaemonDeploymentRec thisDaemonDeployment ();

	String gitVersion ();

}
