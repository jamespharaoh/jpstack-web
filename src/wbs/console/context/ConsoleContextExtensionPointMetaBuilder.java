package wbs.console.context;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.experimental.Accessors;

import wbs.console.annotations.ConsoleMetaModuleBuilderHandler;
import wbs.console.module.ConsoleMetaModuleImpl;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;

@Accessors (fluent = true)
@PrototypeComponent ("consoleContextExtensionPointMetaBuilder")
@ConsoleMetaModuleBuilderHandler
public
class ConsoleContextExtensionPointMetaBuilder {

	// prototype dependencies

	@Inject
	Provider<ConsoleContextNestedExtensionPoint> nestedExtensionPointProvider;

	// builder

	@BuilderParent
	ConsoleContextMetaBuilderContainer contextMetaBuilderContainer;

	@BuilderSource
	ConsoleContextExtensionPointSpec contextExtensionPointSpec;

	@BuilderTarget
	ConsoleMetaModuleImpl consoleMetaModule;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		consoleMetaModule.addExtensionPoint (
			nestedExtensionPointProvider.get ()

			.name (
				contextExtensionPointSpec.name ())

			.parentExtensionPointName (
				contextMetaBuilderContainer.extensionPointName ()));

	}

}
