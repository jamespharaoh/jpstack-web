package wbs.console.context;

import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleMetaModuleBuilderComponent;
import wbs.console.module.ConsoleMetaModuleImplementation;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@PrototypeComponent ("consoleContextExtensionPointMetaBuilder")
public
class ConsoleContextExtensionPointMetaBuilder
	implements ConsoleMetaModuleBuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <ConsoleContextNestedExtensionPoint>
		nestedExtensionPointProvider;

	// builder

	@BuilderParent
	ConsoleContextMetaBuilderContainer contextMetaBuilderContainer;

	@BuilderSource
	ConsoleContextExtensionPointSpec contextExtensionPointSpec;

	@BuilderTarget
	ConsoleMetaModuleImplementation consoleMetaModule;

	// build

	@Override
	@BuildMethod
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

			consoleMetaModule.addExtensionPoint (
				nestedExtensionPointProvider.provide (
					taskLogger)

				.name (
					contextExtensionPointSpec.name ())

				.parentExtensionPointName (
					contextMetaBuilderContainer.extensionPointName ()));

		}

	}

}
