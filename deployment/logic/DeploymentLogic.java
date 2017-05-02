package wbs.platform.deployment.logic;

import wbs.framework.database.Transaction;

import wbs.platform.deployment.model.ApiDeploymentRec;
import wbs.platform.deployment.model.ConsoleDeploymentRec;
import wbs.platform.deployment.model.DaemonDeploymentRec;

public
interface DeploymentLogic {

	//AgentDeploymentRec thisAgentDeployment ();

	ApiDeploymentRec thisApiDeployment (
			Transaction parentTransaction);

	ConsoleDeploymentRec thisConsoleDeployment (
			Transaction parentTransaction);

	DaemonDeploymentRec thisDaemonDeployment (
			Transaction parentTransaction);

	String gitVersion ();

}
