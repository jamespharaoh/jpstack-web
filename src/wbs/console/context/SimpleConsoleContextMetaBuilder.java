package wbs.console.context;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.collect.ImmutableList;

import wbs.console.annotations.ConsoleMetaModuleBuilderHandler;
import wbs.console.module.ConsoleMetaModuleImpl;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.Builder.MissingBuilderBehaviour;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;

@PrototypeComponent ("simpleConsoleContextMetaBuilder")
@ConsoleMetaModuleBuilderHandler
public
class SimpleConsoleContextMetaBuilder {

	// prototype dependencies

	@Inject
	Provider<ConsoleContextRootExtensionPoint> rootExtensionPointProvider;

	// builder

	@BuilderParent
	ConsoleContextMetaBuilderContainer container;

	@BuilderSource
	SimpleConsoleContextSpec spec;

	@BuilderTarget
	ConsoleMetaModuleImpl metaModule;

	// state

	String contextName;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		setDefaults ();

		// extension point

		metaModule.addExtensionPoint (
			rootExtensionPointProvider.get ()

			.name (
				contextName)

			.contextTypeNames (
				ImmutableList.<String>of (
					contextName))

			.contextLinkNames (
				ImmutableList.<String>of ())

			.parentContextNames (
				ImmutableList.<String>of (
					contextName)));

		// descend

		ConsoleContextMetaBuilderContainer nextContainer =
			new ConsoleContextMetaBuilderContainer ()

			.structuralName (
				contextName)

			.extensionPointName (
				contextName);

		builder.descend (
			nextContainer,
			spec.children (),
			metaModule,
			MissingBuilderBehaviour.ignore);

	}

	void setDefaults () {

		contextName =
			spec.name ();

	}

}
