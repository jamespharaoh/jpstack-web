package wbs.console.context;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;
import lombok.experimental.Accessors;

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
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
@PrototypeComponent ("consoleContextSectionMetaBuilder")
public
class ConsoleContextSectionMetaBuilder
	implements ConsoleMetaModuleBuilderComponent {

	// singleton dependenciesa

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <ConsoleContextLink> contextLinkProvider;

	@PrototypeDependency
	ComponentProvider <ConsoleContextRootExtensionPoint>
		rootExtensionPointProvider;

	// builder

	@BuilderParent
	ConsoleContextMetaBuilderContainer container;

	@BuilderSource
	ConsoleContextSectionSpec spec;

	@BuilderTarget
	ConsoleMetaModuleImplementation consoleMetaModule;

	// state

	String structuralName;
	String namePart;
	String label;

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

			// link to parent

			consoleMetaModule.addContextLink (
				contextLinkProvider.provide (
					taskLogger)

				.localName (
					spec.name ())

				.linkName (
					structuralName)

				.label (
					label)

				.extensionPointName (
					container.extensionPointName ())

				.tabLocation (
					spec.name ()));

			// extension point for children

			consoleMetaModule.addExtensionPoint (
				rootExtensionPointProvider.provide (
					taskLogger)

				.name (
					"section:" + structuralName)

				.contextTypeNames (
					ImmutableList.<String>of (
						structuralName))

				.contextLinkNames (
					ImmutableList.<String>of (
						structuralName))

				.parentContextNames (
					ImmutableList.<String>of ())

			);

			// context hints

			consoleMetaModule.addContextHint (
				new ConsoleContextHint ()

				.linkName (
					structuralName)

				.singular (
					true)

				.plural (
					false)

			);

			// build children

			ConsoleContextMetaBuilderContainer nextContainer =
				new ConsoleContextMetaBuilderContainer ()

				.structuralName (
					structuralName)

				.extensionPointName (
					"section:" + structuralName);

			builder.descend (
				taskLogger,
				nextContainer,
				spec.children (),
				consoleMetaModule,
				MissingBuilderBehaviour.ignore);

		}

	}

	// defaults

	void setDefaults () {

		structuralName =
			stringFormat (
				"%s.%s",
				container.structuralName (),
				spec.name ());

		namePart =
			spec.name ();

		label =
			ifNull (
				spec.label (),
				capitalise (
					camelToSpaces (
						namePart)));

	}

}
