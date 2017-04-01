package wbs.platform.deployment.model;

import java.util.List;

public
interface DaemonDeploymentDaoMethods {

	List <DaemonDeploymentRec> findByHostNotDeleted (
			String host);

}
