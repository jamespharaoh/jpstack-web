package wbs.services.messagetemplate.console;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.HiddenComponent;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.UninitializedDependency;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("messageTemplateEntryValueSettingsFieldsProvider")
@HiddenComponent
public
class MessageTemplateEntryValueSettingsFieldsProvider
	implements ComponentFactory <MessageTemplateEntryValueFieldsProvider> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// uninitialized dependencies

	@UninitializedDependency
	Provider <MessageTemplateEntryValueFieldsProvider>
		messageTemplateEntryValueFieldsProvider;

	// implementation

	@Override
	public
	MessageTemplateEntryValueFieldsProvider makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

			return messageTemplateEntryValueFieldsProvider.get ()

				.setMode (
					"settings");

		}


	}

}
