package wbs.platform.deployment.model;

import java.util.List;

public
interface ConsoleDeploymentDaoMethods {

	List <ConsoleDeploymentRec> findByHostNotDeleted (
			String host);

}
