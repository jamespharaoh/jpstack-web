package wbs.console.context;

import javax.inject.Provider;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;

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
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("simpleConsoleContextMetaBuilder")
public
class SimpleConsoleContextMetaBuilder
	implements ConsoleMetaModuleBuilderComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <ConsoleContextRootExtensionPoint> rootExtensionPointProvider;

	// builder

	@BuilderParent
	ConsoleContextMetaBuilderContainer container;

	@BuilderSource
	SimpleConsoleContextSpec spec;

	@BuilderTarget
	ConsoleMetaModuleImplementation metaModule;

	// state

	String contextTypeName;

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

			// extension point

			metaModule.addExtensionPoint (
				rootExtensionPointProvider.get ()

				.name (
					contextTypeName)

				.contextTypeNames (
					ImmutableList.<String>of (
						contextTypeName))

				.contextLinkNames (
					ImmutableList.<String>of (
						contextTypeName))

				.parentContextNames (
					ImmutableList.<String>of (
						contextTypeName)));

			// context hints

			metaModule.addContextHint (
				new ConsoleContextHint ()

				.linkName (
					contextTypeName)

				.singular (
					true)

				.plural (
					false)

			);

			// descend

			ConsoleContextMetaBuilderContainer nextContainer =
				new ConsoleContextMetaBuilderContainer ()

				.structuralName (
					contextTypeName)

				.extensionPointName (
					contextTypeName);

			builder.descend (
				taskLogger,
				nextContainer,
				spec.children (),
				metaModule,
				MissingBuilderBehaviour.ignore);

		}

	}

	void setDefaults () {

		contextTypeName =
			spec.name ();

	}

}
