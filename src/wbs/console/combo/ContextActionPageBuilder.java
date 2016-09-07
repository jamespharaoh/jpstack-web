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

@PrototypeComponent ("contextActionPageBuilder")
@ConsoleModuleBuilderHandler
public
class ContextActionPageBuilder <
	ObjectType extends Record <ObjectType>
> {

	// singleton dependencies

	@SingletonDependency
	ConsoleMetaManager consoleMetaManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <ConsoleFile> consoleFile;

	// builder

	@BuilderSource
	ContextActionPageSpec contextActionPageSpec;

	@BuilderParent
	ConsoleContextBuilderContainer <ObjectType> container;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	String name;
	String fileName;
	String actionName;
	String responderName;
	String responderBeanName;

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

			consoleModule.addContextFile (
				fileName,
				consoleFile.get ()
					.getResponderName (responderName)
					.postActionName (actionName),
				resolvedExtensionPoint.contextTypeNames ());

		}

		consoleModule.addResponder (
			responderName,
			consoleModule.beanResponder (
				responderBeanName));

	}

	// defaults

	void setDefaults () {

		name =
			contextActionPageSpec.name ();

		fileName =
			ifNull (
				contextActionPageSpec.fileName (),
				stringFormat (
					"%s.%s",
					container.pathPrefix (),
					name));

		actionName =
			ifNull (
				contextActionPageSpec.actionName (),
				stringFormat (
					"%s%sAction",
					container.existingBeanNamePrefix (),
					capitalise (
						name)));

		responderName =
			ifNull (
				contextActionPageSpec.responderName (),
				stringFormat (
					"%s%sResponder",
					container.newBeanNamePrefix (),
					capitalise (
						name)));

		responderBeanName =
			ifNull (
				contextActionPageSpec.responderBeanName (),
				stringFormat (
					"%s%sResponder",
					container.existingBeanNamePrefix (),
					capitalise (
						name)));

	}

}
