package wbs.platform.feature.console;

import lombok.NonNull;

import wbs.console.feature.FeatureChecker;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;

import wbs.platform.feature.console.FeatureConsoleHelper;
import wbs.platform.feature.model.FeatureRec;

@SingletonComponent ("featureChecker")
public
class FeatureCheckerImplementation
	implements FeatureChecker {

	// singleton dependencies

	@SingletonDependency
	FeatureConsoleHelper featureHelper;

	@SingletonDependency
	UserPrivChecker privChecker;

	// public implementation

	@Override
	public
	boolean checkFeatureAccess (
			@NonNull String featureCode) {

		FeatureRec feature =
			featureHelper.findByCodeRequired (
				GlobalId.root,
				featureCode);

		return privChecker.canRecursive (
			feature,
			"view");

	}


}
