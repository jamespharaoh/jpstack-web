package wbs.console.feature;

import wbs.framework.logging.TaskLogger;

public
interface FeatureChecker {

	boolean checkFeatureAccess (
			TaskLogger parentTaskLogger,
			String featureName);

}
