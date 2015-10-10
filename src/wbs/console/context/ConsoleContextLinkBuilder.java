package wbs.console.context;

import javax.inject.Inject;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleImpl;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;

@PrototypeComponent ("consoleContextLinkBuilder")
@ConsoleModuleBuilderHandler
public
class ConsoleContextLinkBuilder {

	// dependencies

	@Inject
	ConsoleMetaManager consoleMetaManager;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer container;

	@BuilderSource
	ConsoleContextLinkSpec spec;

	@BuilderTarget
	ConsoleModuleImpl consoleModule;

	// state

	String name;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		setDefaults ();

		for (ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
				: consoleMetaManager.resolveExtensionPoint (
					container.extensionPointName ())) {

			buildTabLocation (
				resolvedExtensionPoint);

		}

	}

	void buildTabLocation (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		consoleModule.addTabLocation (
			"end",
			name,
			resolvedExtensionPoint.contextTypeNames ());

	}

	// defaults

	void setDefaults () {

		name =
			spec.localName ();

	}

}
