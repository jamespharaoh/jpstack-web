package wbs.console.feature;

import wbs.console.priv.UserPrivChecker;

import wbs.framework.database.Transaction;

public
interface FeatureChecker {

	boolean checkFeatureAccess (
			Transaction parentTransaction,
			UserPrivChecker privChecker,
			String featureName);

}
