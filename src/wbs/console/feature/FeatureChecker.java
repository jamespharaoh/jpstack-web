package wbs.console.feature;

import wbs.console.priv.UserPrivChecker;

import wbs.framework.logging.TaskLogger;

public
interface FeatureChecker {

	boolean checkFeatureAccess (
			TaskLogger parentTaskLogger,
			UserPrivChecker privChecker,
			String featureName);

}
