package wbs.console.context;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

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

@PrototypeComponent ("consoleContextSectionComponentBuilder")
public
class ConsoleContextSectionComponentBuilder
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
	ConsoleContextSectionSpec spec;

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
					stringFormat (
						"%s.%s",
						parentContext.structuralName (),
						spec.name ()))

				.pathPrefix (
					stringFormat (
						"%s.%s",
						parentContext.structuralName (),
						spec.name ()))

				.newComponentNamePrefix (
					stringFormat (
						"%s%s",
						parentContext.newComponentNamePrefix (),
						capitalise (
							spec.name ())))

				.existingComponentNamePrefix (
					stringFormat (
						"%s%s",
						parentContext.existingComponentNamePrefix (),
						capitalise (
							ifNull (
								spec.aliasOf (),
								spec.name ()))))

				.friendlyName (
					stringFormat (
						"%s %s",
						camelToSpaces (
							parentContext.existingComponentNamePrefix ()),
						camelToSpaces (
							ifNull (
								spec.aliasOf (),
								spec.name ()))))

				.objectType (
					parentContext.objectType ())

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
