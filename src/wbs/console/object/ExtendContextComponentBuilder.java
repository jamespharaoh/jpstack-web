package wbs.console.object;

import static wbs.utils.string.StringUtils.camelToHyphen;

import lombok.NonNull;

import wbs.console.component.ConsoleComponentBuilderComponent;
import wbs.console.component.ConsoleComponentBuilderContext;
import wbs.console.component.ConsoleComponentBuilderContextImplementation;

import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.component.registry.ComponentRegistryBuilder;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("extendContextComponentBuilder")
public
class ExtendContextComponentBuilder
	implements ConsoleComponentBuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@ClassSingletonDependency
	LogContext logContext;

	// builder

	@BuilderParent
	ConsoleComponentBuilderContext parentContext;

	@BuilderSource
	ExtendContextSpec spec;

	@BuilderTarget
	ComponentRegistryBuilder target;

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

			ConsoleComponentBuilderContext childContext =
				new ConsoleComponentBuilderContextImplementation ()

				.consoleModule (
					parentContext.consoleModule ())

				.structuralName (
					spec.baseName ())

				.pathPrefix (
					spec.name ())

				.newComponentNamePrefix (
					spec.componentName ())

				.existingComponentNamePrefix (
					spec.componentName ())

				.friendlyName (
					spec.friendlyName ())

				.objectType (
					camelToHyphen (
						spec.objectName ()))

			;

			builder.descend (
				taskLogger,
				childContext,
				spec.children (),
				target,
				MissingBuilderBehaviour.error);

		}

	}

}
