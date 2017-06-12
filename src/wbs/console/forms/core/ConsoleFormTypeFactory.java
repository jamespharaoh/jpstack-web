package wbs.console.forms.core;

import static wbs.utils.etc.LogicUtils.ifNotNullThenElse;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import javax.inject.Provider;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.types.FieldsProvider;
import wbs.console.forms.types.FormType;
import wbs.console.helper.core.ConsoleHelper;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.UninitializedDependency;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
public
class ConsoleFormTypeFactory <Container>
	implements ComponentFactory <ConsoleFormType <Container>> {

	// singleton components

	@SingletonDependency
	ConsoleFormBuilder consoleFormBuilder;

	@ClassSingletonDependency
	LogContext logContext;

	// uninitalized components

	@UninitializedDependency
	Provider <ConsoleFormTypeImplementation <Container>>
		consoleFormTypeProvider;

	// properties

	@Getter @Setter
	ConsoleHelper <?> consoleHelper;

	@Getter @Setter
	Class <Container> containerClass;

	@Getter @Setter
	String formName;

	@Getter @Setter
	FormType formType;

	@Getter @Setter
	FieldsProvider <Container, ?> fieldsProvider;

	// implementation

	@Override
	public
	ConsoleFormType <Container> makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

			return consoleFormTypeProvider.get ()

				.containerClass (
					genericCastUnchecked (
						ifNotNullThenElse (
							consoleHelper,
							() -> consoleHelper.objectClass (),
							() -> containerClass)))

				.formName (
					formName)

				.formType (
					formType)

				.fieldsProvider (
					fieldsProvider)

			;

		}

	}

}
