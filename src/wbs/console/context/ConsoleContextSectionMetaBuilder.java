package wbs.console.context;

import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.camelToSpaces;
import static wbs.framework.utils.etc.StringUtils.capitalise;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.experimental.Accessors;

import com.google.common.collect.ImmutableList;

import wbs.console.annotations.ConsoleMetaModuleBuilderHandler;
import wbs.console.module.ConsoleMetaModuleImplementation;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;

@Accessors (fluent = true)
@PrototypeComponent ("consoleContextSectionMetaBuilder")
@ConsoleMetaModuleBuilderHandler
public
class ConsoleContextSectionMetaBuilder {

	// prototype dependencies

	@Inject
	Provider<ConsoleContextLink> contextLinkProvider;

	@Inject
	Provider<ConsoleContextRootExtensionPoint> rootExtensionPointProvider;

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
	public
	void build (
			Builder builder) {

		setDefaults ();

		// link to parent

		consoleMetaModule.addContextLink (
			contextLinkProvider.get ()

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
			rootExtensionPointProvider.get ()

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
			nextContainer,
			spec.children (),
			consoleMetaModule,
			MissingBuilderBehaviour.ignore);

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
