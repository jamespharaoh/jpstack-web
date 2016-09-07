package wbs.console.context;

import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.record.Record;

@Accessors (fluent = true)
@PrototypeComponent ("consoleContextExtensionPointBuilder")
@ConsoleModuleBuilderHandler
public
class ConsoleContextExtensionPointBuilder <
	ObjectType extends Record <ObjectType>
> {

	// singleton dependencies

	@SingletonDependency
	ConsoleMetaManager consoleMetaManager;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer <ObjectType> container;

	@BuilderSource
	ConsoleContextExtensionPointSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// build

	@BuildMethod
	public
	void build (
			@NonNull Builder builder) {

		for (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
				: consoleMetaManager.resolveExtensionPoint (
					container.extensionPointName ())
			) {

			consoleModule.addTabLocation (
				container.tabLocation (),
				spec.name (),
				resolvedExtensionPoint.contextTypeNames ());

		}

	}

}
