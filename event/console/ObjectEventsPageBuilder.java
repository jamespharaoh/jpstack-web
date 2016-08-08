package wbs.platform.event.console;

import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.console.helper.ConsoleHelper;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleMetaModuleImplementation;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.part.PagePart;
import wbs.console.responder.ConsoleFile;
import wbs.console.tab.ConsoleContextTab;
import wbs.console.tab.TabContextResponder;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.record.Record;

@PrototypeComponent ("objectEventsPageBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectEventsPageBuilder<
	ObjectType extends Record<ObjectType>
> {

	// dependencies

	@Inject
	ConsoleMetaManager consoleMetaManager;

	@Inject
	EventConsoleLogic eventConsoleModule;

	// prototype dependencies

	@Inject
	Provider<ConsoleFile> consoleFile;

	@Inject
	Provider<ConsoleContextTab> contextTab;

	@Inject
	Provider<TabContextResponder> tabContextResponder;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer<ObjectType> container;

	@BuilderSource
	ObjectEventsPageSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	ConsoleHelper<ObjectType> consoleHelper;

	String privKey;
	String tabName;
	String fileName;
	String responderName;

	// build meta

	public
	void buildMeta (
			@NonNull ConsoleMetaModuleImplementation consoleMetaModule) {

	}

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

			buildTab (
				resolvedExtensionPoint);

			buildFile (
				resolvedExtensionPoint);

		}

		buildResponder ();

	}

	void buildTab (
			@NonNull ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		consoleModule.addContextTab (
			"end",
			contextTab.get ()
				.name (tabName)
				.defaultLabel ("Events")
				.localFile (fileName)
				.privKeys (Collections.singletonList (privKey)),
			resolvedExtensionPoint.contextTypeNames ());

	}

	void buildFile (
			@NonNull ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		consoleModule.addContextFile (
			fileName,
			consoleFile.get ()
				.getResponderName (responderName)
				.privName (privKey),
			resolvedExtensionPoint.contextTypeNames ());

	}

	void buildResponder () {

		Provider<PagePart> eventsPartFactory =
			eventConsoleModule.makeEventsPartFactory (
				consoleHelper);

		consoleModule.addResponder (
			responderName,
			tabContextResponder.get ()

				.tab (
					tabName)

				.title (
					stringFormat (
						"%s events",
						capitalise (consoleHelper.friendlyName () + " events")))

				.pagePartFactory (
					eventsPartFactory));

	}

	void setDefaults () {

		consoleHelper =
			container.consoleHelper ();

		privKey =
			ifNull (
				spec.privKey (),
				stringFormat (
					"%s.manage",
					consoleHelper.objectName ()));

		tabName =
			ifNull (
				spec.tabName (),
				stringFormat (
					"%s.events",
					container.pathPrefix ()));

		fileName =
			ifNull (
				spec.fileName (),
				stringFormat (
					"%s.events",
					container.pathPrefix ()));

		responderName =
			ifNull (
				spec.responderName (),
				stringFormat (
					"%sEventsResponder",
					container.newBeanNamePrefix ()));

	}

}
