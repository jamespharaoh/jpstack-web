package wbs.integrations.fonix.foreignapi;

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

@PrototypeComponent ("fonixMessageSender")
public
class FonixMessageSender
	extends GenericHttpSender <
		FonixMessageSender,
		FonixMessageSendRequest,
		FonixMessageSendResponse,
		FonixMessageSenderHelper
	> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <FonixMessageSenderHelper> fonixMessageSenderHelperProvider;

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
				fonixMessageSenderHelperProvider.get ());

		}

	}

}
