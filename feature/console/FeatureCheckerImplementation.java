package wbs.platform.feature.console;

import lombok.NonNull;

import wbs.console.feature.FeatureChecker;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.feature.model.FeatureRec;

@SingletonComponent ("featureChecker")
public
class FeatureCheckerImplementation
	implements FeatureChecker {

	// singleton dependencies

	@SingletonDependency
	FeatureConsoleHelper featureHelper;

	@ClassSingletonDependency
	LogContext logContext;

	// public implementation

	@Override
	public
	boolean checkFeatureAccess (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull UserPrivChecker privChecker,
			@NonNull String featureCode) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"checkFeatureAccess");

		) {

			FeatureRec feature =
				featureHelper.findByCodeRequired (
					GlobalId.root,
					featureCode);

			return privChecker.canRecursive (
				taskLogger,
				feature,
				"view");

		}

	}


}
