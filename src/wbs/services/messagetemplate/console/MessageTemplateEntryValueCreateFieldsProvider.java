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

@PrototypeComponent ("messageTemplateEntryValueCreateFieldsProvider")
@HiddenComponent
public
class MessageTemplateEntryValueCreateFieldsProvider
	implements ComponentFactory <MessageTemplateEntryValueFieldsProvider> {

	// singleton compomenents

	@ClassSingletonDependency
	LogContext logContext;

	// uninitialized components

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
					"create");

		}

	}

}
