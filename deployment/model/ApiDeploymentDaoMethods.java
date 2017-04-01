package wbs.platform.deployment.model;

import java.util.List;

public
interface ApiDeploymentDaoMethods {

	List <ApiDeploymentRec> findByHostNotDeleted (
			String host);

}
