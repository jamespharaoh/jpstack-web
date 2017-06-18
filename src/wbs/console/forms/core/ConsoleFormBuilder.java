package wbs.console.forms.core;

import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.forms.types.FormField;
import wbs.console.forms.types.FormItem;
import wbs.console.helper.core.ConsoleHelper;

import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.BuilderFactory;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.StrongPrototypeDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("consoleFormBuilder")
public
class ConsoleFormBuilder {

	// singleton components

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@StrongPrototypeDependency
	Map <Class <?>, ComponentProvider <ConsoleFormBuilderComponent>>
		builderProviders;

	@StrongPrototypeDependency
	Provider <BuilderFactory <?, TaskLogger>> builderFactoryProvider;

	// state

	Builder <TaskLogger> builder;

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

			builder =
				builderFactoryProvider.get ()

				.contextClass (
					TaskLogger.class)

				.addBuilders (
					taskLogger,
					builderProviders)

				.create (
					taskLogger)

			;

		}

	}

	// implementation

	public <Container extends Record <Container>>
	FormFieldSet <Container> buildFormFieldSet (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ConsoleHelper <Container> consoleHelper,
			@NonNull String fieldSetName,
			@NonNull List <?> formFieldSpecs) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildFormFieldSet");

		) {

			ConsoleFormBuilderContext formFieldBuilderContext =
				new FormFieldBuilderContextImplementation ()

				.containerClass (
					consoleHelper.objectClass ())

				.consoleHelper (
					consoleHelper);

			FormFieldSetImplementation <Container> formFieldSet =
				new FormFieldSetImplementation <Container> ()

				.name (
					fieldSetName)

				.containerClass (
					consoleHelper.objectClass ())

			;

			builder.descend (
				taskLogger,
				formFieldBuilderContext,
				formFieldSpecs,
				formFieldSet,
				MissingBuilderBehaviour.error);

			initFormFieldSet (
				taskLogger,
				formFieldSet);

			return formFieldSet;

		}

	}

	public <Container>
	FormFieldSet <Container> buildFormFieldSet (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Class <Container> containerClass,
			@NonNull String fieldSetName,
			@NonNull List <Object> formFieldSpecs) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildFormFieldSet");

		) {

			ConsoleFormBuilderContext formFieldBuilderContext =
				new FormFieldBuilderContextImplementation ()

				.containerClass (
					containerClass)

			;

			FormFieldSetImplementation <Container> formFieldSet =
				new FormFieldSetImplementation <Container> ()

				.name (
					fieldSetName)

				.containerClass (
					containerClass)

			;

			builder.descend (
				taskLogger,
				formFieldBuilderContext,
				formFieldSpecs,
				formFieldSet,
				MissingBuilderBehaviour.error);

			initFormFieldSet (
				taskLogger,
				formFieldSet);

			return formFieldSet;

		}

	}

	// private implementation

	private
	void initFormFieldSet (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormFieldSetImplementation <?> formFieldSet) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"initFormFieldSet");

		) {

			for (
				FormItem <?> formItem
					: formFieldSet.formItems ()
			) {

				formItem.init (
					formFieldSet.name ());

			}

			for (
				FormField <?, ?, ?, ?> formField
					: formFieldSet.formFields ()
			) {

				if (formField.fileUpload ()) {
					formFieldSet.fileUpload (true);
				}

			}

			if (formFieldSet.fileUpload () == null) {
				formFieldSet.fileUpload (false);
			}

		}

	}

}
