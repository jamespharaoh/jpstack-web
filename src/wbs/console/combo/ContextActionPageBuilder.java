package wbs.console.combo;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleImpl;
import wbs.console.responder.ConsoleFile;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;

@PrototypeComponent ("contextActionPageBuilder")
@ConsoleModuleBuilderHandler
public
class ContextActionPageBuilder {

	// dependencies

	@Inject
	ConsoleMetaManager consoleMetaManager;

	// prototype dependencies

	@Inject
	Provider<ConsoleFile> consoleFile;

	// builder

	@BuilderSource
	ContextActionPageSpec contextActionPageSpec;

	@BuilderParent
	ConsoleContextBuilderContainer container;

	@BuilderTarget
	ConsoleModuleImpl consoleModule;

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
			Builder builder) {

		setDefaults ();

		for (ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
				: consoleMetaManager.resolveExtensionPoint (
					container.extensionPointName ())) {

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
					capitalise (name)));

		responderName =
			ifNull (
				contextActionPageSpec.responderName (),
				stringFormat (
					"%s%sResponder",
					container.newBeanNamePrefix (),
					capitalise (name)));

		responderBeanName =
			ifNull (
				contextActionPageSpec.responderBeanName (),
				stringFormat (
					"%s%sResponder",
					container.existingBeanNamePrefix (),
					capitalise (name)));

	}

}
