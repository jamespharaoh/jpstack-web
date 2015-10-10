package wbs.console.combo;

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

@PrototypeComponent ("contextFileBuilder")
@ConsoleModuleBuilderHandler
public
class ContextFileBuilder {

	// prototype dependencies

	@Inject
	Provider<ConsoleFile> consoleFile;

	@Inject
	ConsoleMetaManager consoleMetaManager;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer container;

	@BuilderSource
	ContextFileSpec spec;

	@BuilderTarget
	ConsoleModuleImpl consoleModule;

	// state

	String name;
	String fileName;
	String getResponderName;
	String getActionName;
	String postActionName;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		setDefaults ();

		for (ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
				: consoleMetaManager.resolveExtensionPoint (
					container.extensionPointName ())) {

			buildContextFile (
				resolvedExtensionPoint);

		}

	}

	void buildContextFile (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		consoleModule.addContextFile (
			fileName,
			consoleFile.get ()
				.getResponderName (getResponderName)
				.getActionName (getActionName)
				.postActionName (postActionName),
			resolvedExtensionPoint.contextTypeNames ());

	}

	// defaults

	void setDefaults () {

		name =
			spec.name ();

		fileName =
			spec.fileName ();

		getResponderName =
			spec.getResponderName ();

		getActionName =
			spec.getActionName ();

		postActionName =
			spec.postActionName ();

		fileName =
			ifNull (
				spec.fileName (),
				stringFormat (
					"%s.%s",
					container.pathPrefix (),
					name));

	}

}
