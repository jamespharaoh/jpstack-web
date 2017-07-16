package wbs.console.forms.core;

import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalMapRequiredOrNull;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.StrongPrototypeDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
public
class StaticFieldsProviderFactory <Container, Parent>
	implements ComponentFactory <StaticFieldsProvider <Container, Parent>> {

	// singleton dependencies

	@SingletonDependency
	ConsoleFormBuilder consoleFormBuilder;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@StrongPrototypeDependency
	ComponentProvider <StaticFieldsProvider <Container, Parent>>
		staticFieldsProviderProvider;

	// properties

	@Getter @Setter
	Class <Container> containerClass;

	@Getter @Setter
	Class <Parent> parentClass;

	@Getter @Setter
	String name;

	@Getter @Setter
	List <Object> columnFieldSpecs;

	@Getter @Setter
	List <Object> rowFieldSpecs;

	// implementation

	@Override
	public
	StaticFieldsProvider <Container, Parent> makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

			return staticFieldsProviderProvider.provide (
				taskLogger,
				staticFieldsProvider ->
					staticFieldsProvider

				.containerClass (
					containerClass)

				.parentClass (
					parentClass)

				.columnFields (
					optionalMapRequiredOrNull (
						optionalFromNullable (
							columnFieldSpecs),
						columnFieldSpecsNested ->
							consoleFormBuilder.buildFormFieldSet (
								taskLogger,
								containerClass,
								stringFormat (
									"%s.columns",
									name),
								columnFieldSpecsNested)))

				.rowFields (
					optionalMapRequiredOrNull (
						optionalFromNullable (
							rowFieldSpecs),
						rowFieldSpecsNested ->
							consoleFormBuilder.buildFormFieldSet (
								taskLogger,
								containerClass,
								stringFormat (
									"%s.rows",
									name),
								rowFieldSpecsNested)))

			);

		}

	}

}
