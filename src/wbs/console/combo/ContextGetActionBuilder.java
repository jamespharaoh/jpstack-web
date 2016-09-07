package wbs.console.combo;

import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.responder.ConsoleFile;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;

@PrototypeComponent ("contextGetActionBuilder")
@ConsoleModuleBuilderHandler
public
class ContextGetActionBuilder <
	ObjectType extends Record <ObjectType>
> {

	// singleton dependencies

	@SingletonDependency
	ConsoleMetaManager consoleMetaManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <ConsoleFile> consoleFile;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer <ObjectType> container;

	@BuilderSource
	ContextGetActionSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	String name;
	String contextFileName;
	String actionName;

	// build

	@BuildMethod
	public
	void build (
			@NonNull Builder builder) {

		setDefaults ();

		for (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
				: consoleMetaManager.resolveExtensionPoint (
					container.extensionPointName ())
		) {

			buildContextFile (
				resolvedExtensionPoint);

		}

	}

	void buildContextFile (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		consoleModule.addContextFile (
			contextFileName,
			consoleFile.get ()
				.getActionName (actionName),
			resolvedExtensionPoint.contextTypeNames ());

	}

	void setDefaults () {

		name =
			spec.name ();

		contextFileName =
			ifNull (
				spec.contextFileName (),
				stringFormat (
					"%s.%s",
					container.pathPrefix (),
					name));

		actionName =
			ifNull (
				spec.actionName (),
				stringFormat (
					"%s%sAction",
					container.existingBeanNamePrefix (),
					capitalise (
						name)));

	}

}
