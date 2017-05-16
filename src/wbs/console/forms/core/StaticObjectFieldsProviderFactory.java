package wbs.console.forms.core;

import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalMapRequiredOrNull;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import javax.inject.Provider;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.helper.core.ConsoleHelper;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.UninitializedDependency;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
public
class StaticObjectFieldsProviderFactory <
	Container extends Record <Container>,
	Parent extends Record <Parent>
>
	implements ComponentFactory <StaticFieldsProvider <Container, Parent>> {

	// singleton dependencies

	@SingletonDependency
	ConsoleFormBuilder consoleFormBuilder;

	@ClassSingletonDependency
	LogContext logContext;

	// uninitialized dependencies

	@UninitializedDependency
	Provider <StaticFieldsProvider <Container, Parent>>
		staticFieldsProviderProvider;

	// properties

	@Getter @Setter
	ConsoleHelper <Container> consoleHelper;

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

			Class <Parent> parentClass =
				genericCastUnchecked (
					ifThenElse (
						consoleHelper.parentTypeIsFixed (),
						() -> consoleHelper.parentClassRequired (),
						() -> null));

			return staticFieldsProviderProvider.get ()

				.containerClass (
					consoleHelper.objectClass ())

				.parentClass (
					parentClass)

				.columnFields (
					optionalMapRequiredOrNull (
						optionalFromNullable (
							columnFieldSpecs),
						columnFieldSpecsNested ->
							consoleFormBuilder.buildFormFieldSet (
								taskLogger,
								consoleHelper,
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
								consoleHelper,
								stringFormat (
									"%s.rows",
									name),
								rowFieldSpecsNested)))

			;

		}

	}

}
