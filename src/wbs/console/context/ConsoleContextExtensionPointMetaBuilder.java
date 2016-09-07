package wbs.console.context;

import javax.inject.Provider;

import lombok.experimental.Accessors;

import wbs.console.annotations.ConsoleMetaModuleBuilderHandler;
import wbs.console.module.ConsoleMetaModuleImplementation;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;

@Accessors (fluent = true)
@PrototypeComponent ("consoleContextExtensionPointMetaBuilder")
@ConsoleMetaModuleBuilderHandler
public
class ConsoleContextExtensionPointMetaBuilder {

	// prototype dependencies

	@PrototypeDependency
	Provider <ConsoleContextNestedExtensionPoint> nestedExtensionPointProvider;

	// builder

	@BuilderParent
	ConsoleContextMetaBuilderContainer contextMetaBuilderContainer;

	@BuilderSource
	ConsoleContextExtensionPointSpec contextExtensionPointSpec;

	@BuilderTarget
	ConsoleMetaModuleImplementation consoleMetaModule;

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
