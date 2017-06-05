package wbs.integrations.oxygenate.foreignapi;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.framework.apiclient.GenericHttpSender;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("oxygenateSmsSender")
public
class OxygenateSmsSender
	extends GenericHttpSender <
		OxygenateSmsSender,
		OxygenateSmsSendRequest,
		OxygenateSmsSendResponse,
		OxygenateSmsSendHelper
	> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <OxygenateSmsSendHelper> oxygenateSmsSendHelperProvider;

	// life cycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			helper (
				oxygenateSmsSendHelperProvider.get ());

		}

	}

}
