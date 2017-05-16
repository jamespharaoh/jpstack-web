package wbs.console.forms.core;

import static wbs.utils.etc.TypeUtils.classForNameOrThrow;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.joinWithFullStop;
import static wbs.utils.string.StringUtils.stringFormat;

import lombok.NonNull;

import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.module.ConsoleModuleBuilderComponent;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.module.SimpleConsoleBuilderContainer;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("formFieldSetBuilder")
public
class FormFieldSetBuilder <Container>
	implements ConsoleModuleBuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ConsoleFormBuilder consoleFormBuilder;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// builder

	@BuilderParent
	SimpleConsoleBuilderContainer container;

	@BuilderSource
	FormFieldSetSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// builder implementation

	@BuildMethod
	@Override
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder <TaskLogger> builder) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			if (
				spec.objectName () == null
				&& spec.className () == null
			) {

				throw new RuntimeException (
					stringFormat (
						"Form field set %s ",
						spec.name (),
						"in console module %s ",
						spec.consoleModule ().name (),
						"has neither object name nor class"));

			}

			if (
				spec.objectName () != null
				&& spec.className () != null
			) {

				throw new RuntimeException (
					stringFormat (
						"Form field set %s ",
						spec.name (),
						"in console module %s ",
						spec.consoleModule ().name (),
						"has both object name and class"));

			}

			String fullName =
				joinWithFullStop (
					spec.consoleModule ().name (),
					spec.name ());

			if (spec.objectName () != null) {

				ConsoleHelper <?> consoleHelper =
					objectManager.findConsoleHelperRequired (
						spec.objectName ());

				consoleModule.addFormFieldSet (
					spec.name (),
					consoleFormBuilder.buildFormFieldSet (
						taskLogger,
						consoleHelper,
						fullName,
						spec.formFieldSpecs ()));

			} else {

				Class <Container> containerClass =
					genericCastUnchecked (
						classForNameOrThrow (
							spec.className (),
							() -> new RuntimeException (
								stringFormat (
									"Error getting object class %s ",
									spec.className (),
									"for form field set %s ",
									spec.name (),
									"in console module %s",
									spec.consoleModule ().name ()))));

				consoleModule.addFormFieldSet (
					spec.name (),
					consoleFormBuilder.buildFormFieldSet (
						taskLogger,
						containerClass,
						fullName,
						spec.formFieldSpecs ()));

			}

		}

	}

}
