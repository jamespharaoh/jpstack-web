package wbs.console.forms.context;

import javax.inject.Provider;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import wbs.console.forms.types.FormType;
import wbs.console.module.ConsoleModule;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.UninitializedDependency;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

public
class FormContextBuilderFactory <Container>
	implements ComponentFactory <FormContextBuilder <Container>> {

	// singleton components

	@ClassSingletonDependency
	LogContext logContext;

	// uninitalized components

	@UninitializedDependency
	Provider <FormContextBuilder <Container>> formContextBuilderProvider;

	// properties

	@Getter @Setter
	ConsoleModule consoleModule;

	@Getter @Setter
	Class <Container> objectClass;

	@Getter @Setter
	String formName;

	@Getter @Setter
	String fieldsName;

	// implementation

	@Override
	public
	FormContextBuilder <Container> makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

			return formContextBuilderProvider.get ()

				.objectClass (
					objectClass)

				.formName (
					formName)

				.formType (
					FormType.perform)

				.columnFields (
					consoleModule.formFieldSetRequired (
						fieldsName,
						objectClass))

			;

		}

	}

}
