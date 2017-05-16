package wbs.console.object;

import lombok.NonNull;

import wbs.console.context.ConsoleContextMetaBuilderContainer;
import wbs.console.module.ConsoleMetaModuleBuilderComponent;
import wbs.console.module.ConsoleMetaModuleImplementation;

import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("extendContextMetaBuilder")
public
class ExtendContextMetaBuilder
	implements ConsoleMetaModuleBuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// builder

	@BuilderParent
	ConsoleContextMetaBuilderContainer container;

	@BuilderSource
	ExtendContextSpec spec;

	@BuilderTarget
	ConsoleMetaModuleImplementation metaModule;

	// state

	String name;
	String baseName;
	String extensionPointName;

	// build

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

			setDefaults ();

			ConsoleContextMetaBuilderContainer nextContainer =
				new ConsoleContextMetaBuilderContainer ()

				.structuralName (
					baseName)

				.extensionPointName (
					extensionPointName);

			builder.descend (
				taskLogger,
				nextContainer,
				spec.children (),
				metaModule,
				MissingBuilderBehaviour.ignore);

		}

	}

	// defaults

	void setDefaults () {

		name =
			spec.name ();

		baseName =
			spec.baseName ();

		extensionPointName =
			spec.extensionPointName ();

	}

}
