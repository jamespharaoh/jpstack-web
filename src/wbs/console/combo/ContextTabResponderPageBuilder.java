package wbs.console.combo;

import static wbs.framework.utils.etc.Misc.camelToSpaces;
import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleImpl;
import wbs.console.responder.ConsoleFile;
import wbs.console.tab.ConsoleContextTab;
import wbs.console.tab.TabContextResponder;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;

@PrototypeComponent ("contextTabResponderPageBuilder")
@ConsoleModuleBuilderHandler
public
class ContextTabResponderPageBuilder {

	// dependencies

	@Inject
	ConsoleMetaManager consoleMetaManager;

	// prototype dependencies

	@Inject
	Provider<ConsoleFile> consoleFile;

	@Inject
	Provider<ConsoleContextTab> contextTab;

	@Inject
	Provider<TabContextResponder> tabContextResponder;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer container;

	@BuilderSource
	ContextTabResponderPageSpec spec;

	@BuilderTarget
	ConsoleModuleImpl consoleModule;

	// state

	String name;
	String tabName;
	String tabLabel;
	String fileName;
	String responderName;
	String pageTitle;
	String pagePartName;
	Boolean hideTab;

	// build

	@BuildMethod
	public
	void buildConsoleModule (
			Builder builder) {

		setDefaults ();

		for (ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
				: consoleMetaManager.resolveExtensionPoint (
					container.extensionPointName ())) {

			buildTab (
				resolvedExtensionPoint);


			buildFile (
				resolvedExtensionPoint);

		}

		buildResponder ();

	}

	// private implementation

	void buildTab (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		consoleModule.addContextTab (

			container.tabLocation (),

			contextTab.get ()

				.name (
					tabName)

				.defaultLabel (
					tabLabel)

				.localFile (
					fileName),

			hideTab
				? Collections.<String>emptyList ()
				: resolvedExtensionPoint.contextTypeNames ());

	}

	void buildFile (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		consoleModule.addContextFile (

			fileName,

			consoleFile.get ()

				.getResponderName (
					responderName),

			resolvedExtensionPoint.contextTypeNames ());

	}

	void buildResponder () {

		consoleModule.addResponder (

			responderName,

			tabContextResponder.get ()

				.tab (
					tabName)

				.title (
					pageTitle)

				.pagePartName (
					pagePartName));

	}

	// defaults

	void setDefaults () {

		String name =
			spec.name ();

		tabName =
			ifNull (
				spec.tabName (),
				stringFormat (
					"%s.%s",
					container.pathPrefix (),
					name));

		tabLabel =
			ifNull (
				spec.tabLabel (),
				capitalise (camelToSpaces (name)));

		fileName =
			ifNull (
				spec.fileName (),
				stringFormat (
					"%s.%s",
					container.pathPrefix (),
					name));

		responderName =
			ifNull (
				spec.responderName (),
				stringFormat (
					"%s%sResponder",
					container.newBeanNamePrefix (),
					capitalise (name)));

		pageTitle =
			ifNull (
				spec.pageTitle (),
				capitalise (camelToSpaces (name)));

		pagePartName =
			ifNull (
				spec.pagePartName (),
				stringFormat (
					"%s%sPart",
					container.existingBeanNamePrefix (),
					capitalise (name)));

		hideTab =
			spec.hideTab ();

	}

}
