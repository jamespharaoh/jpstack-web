package wbs.console.forms.core;

import static wbs.utils.collection.MapUtils.mapTransformToMap;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

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
class ConsoleMultiFormTypeFactory <Container>
	implements ComponentFactory <ConsoleMultiFormType <Container>> {

	// singleton components

	@SingletonDependency
	ConsoleFormBuilder consoleFormBuilder;

	@ClassSingletonDependency
	LogContext logContext;

	// uninitalized components

	@UninitializedDependency
	Provider <ConsoleMultiFormTypeImplementation <Container>>
		multiFormContextTypeImplementationProvider;

	// properties

	@Getter @Setter
	String consoleModuleName;

	@Getter @Setter
	ConsoleHelper <?> consoleHelper;

	@Getter @Setter
	Class <Container> containerClass;

	@Getter @Setter
	String formName;

	@Getter @Setter
	FormType formType;

	@Getter @Setter
	Map <String, List <Object>> sectionFields;

	// implementation

	@Override
	public
	ConsoleMultiFormType <Container> makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

			return multiFormContextTypeImplementationProvider.get ()

				.containerClass (
					containerClass)

				.formName (
					formName)

				.formType (
					formType)

				.fieldSets (
					mapTransformToMap (
						sectionFields,
						(sectionName, sectionFields) ->
							sectionName,
						(sectionName, sectionFields) ->
							buildFormFieldSet (
								taskLogger,
								sectionName,
								sectionFields)))

			;

		}

	}

	private
	FormFieldSet <Container> buildFormFieldSet (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String name,
			@NonNull List <Object> fieldSpecs) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildFormFieldSet");

		) {

			if (
				isNotNull (
					consoleHelper)
			) {

				return genericCastUnchecked (
					consoleFormBuilder.buildFormFieldSet (
						taskLogger,
						consoleHelper,
						stringFormat (
							"%s.%s.%s",
							consoleModuleName,
							formName,
							name),
						fieldSpecs));

			} else {

				return consoleFormBuilder.buildFormFieldSet (
					taskLogger,
					containerClass,
					stringFormat (
						"%s.%s.%s",
						consoleModuleName,
						formName,
						name),
					fieldSpecs);

			}

		}

	}

}
