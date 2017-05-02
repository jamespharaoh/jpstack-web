package wbs.console.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.forms.FormField;
import wbs.console.forms.FormFieldBuilderContext;
import wbs.console.forms.FormFieldBuilderContextImplementation;
import wbs.console.forms.FormFieldSet;
import wbs.console.forms.FormFieldSetSpec;
import wbs.console.forms.FormItem;
import wbs.console.helper.core.ConsoleHelper;

import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderFactory;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("consoleModuleBuilder")
public
class ConsoleModuleBuilder
	implements Builder <TaskLogger> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <BuilderFactory <?, TaskLogger>> builderFactoryProvider;

	@PrototypeDependency
	@ConsoleModuleBuilderHandler
	Map <Class <?>, Provider <Object>> consoleModuleBuilders;

	// state

	Builder <TaskLogger> builder;

	// init

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

			builder =
				builderFactoryProvider.get ()

				.contextClass (
					TaskLogger.class)

				.addBuilders (
					taskLogger,
					consoleModuleBuilders)

				.create (
					taskLogger)

			;

		}

	}

	// implementation

	public <Container>
	FormFieldSet <Container> buildFormFieldSet (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ConsoleHelper <?> consoleHelper,
			@NonNull String fieldSetName,
			@NonNull List <Object> formFieldSpecs) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildFormFieldSet");

		) {

			FormFieldBuilderContext formFieldBuilderContext =
				new FormFieldBuilderContextImplementation ()

				.containerClass (
					consoleHelper.objectClass ())

				.consoleHelper (
					consoleHelper);

			FormFieldSet <Container> formFieldSet =
				new FormFieldSet <Container> ();

			builder.descend (
				taskLogger,
				formFieldBuilderContext,
				formFieldSpecs,
				formFieldSet,
				MissingBuilderBehaviour.error);

			for (
				FormItem <?> formItem
					: formFieldSet.formItems ()
			) {

				formItem.init (
					fieldSetName);

			}

			for (
				FormField <?, ?, ?, ?> formField
					: formFieldSet.formFields ()
			) {

				if (formField.fileUpload ()) {

					formFieldSet.fileUpload (
						true);

				}

			}

			if (formFieldSet.fileUpload () == null)
				formFieldSet.fileUpload (false);

			return formFieldSet;

		}

	}

	// builder

	@Override
	public
	void descend (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Object parentObject,
			@NonNull List <?> childObjects,
			@NonNull Object targetObject,
			@NonNull MissingBuilderBehaviour missingBuilderBehaviour) {

		List <Object> firstPass =
			new ArrayList<> ();

		List <Object> secondPass =
			new ArrayList<> ();

		for (
			Object childObject
				: childObjects
		) {

			if (childObject instanceof FormFieldSetSpec) {

				firstPass.add (
					childObject);

			} else {

				secondPass.add (
					childObject);

			}

		}

		builder.descend (
			parentTaskLogger,
			parentObject,
			firstPass,
			targetObject,
			missingBuilderBehaviour);

		builder.descend (
			parentTaskLogger,
			parentObject,
			secondPass,
			targetObject,
			missingBuilderBehaviour);

	}

}
