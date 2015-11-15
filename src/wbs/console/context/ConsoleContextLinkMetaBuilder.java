package wbs.console.context;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.annotations.ConsoleMetaModuleBuilderHandler;
import wbs.console.module.ConsoleMetaModuleImplementation;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;

@PrototypeComponent ("consoleContextLinkMetaBuilder")
@ConsoleMetaModuleBuilderHandler
public
class ConsoleContextLinkMetaBuilder {

	// prototype dependencies

	@Inject
	Provider<ConsoleContextLink> contextLinkProvider;

	// builder

	@BuilderParent
	ConsoleContextMetaBuilderContainer container;

	@BuilderSource
	ConsoleContextLinkSpec spec;

	@BuilderTarget
	ConsoleMetaModuleImplementation consoleMetaModule;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		consoleMetaModule.addContextLink (
			contextLinkProvider.get ()

			.localName (
				spec.localName ())

			.linkName (
				spec.linkName ())

			.label (
				spec.label ())

			.extensionPointName (
				container.extensionPointName ())

			.tabLocation (
				spec.localName ()));

	}

}
