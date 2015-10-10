package wbs.sms.messageset.console;

import static wbs.framework.utils.etc.Misc.camelToSpaces;
import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ConsoleContextPrivLookup;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.console.helper.ConsoleHelper;
import wbs.console.lookup.BooleanLookup;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleMetaModuleImpl;
import wbs.console.module.ConsoleModuleImpl;
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
import wbs.framework.web.Action;
import wbs.framework.web.Responder;
import wbs.platform.core.console.CoreAuthAction;

@PrototypeComponent ("objectSmsMessageSetPageBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectSmsMessageSetPageBuilder {

	// dependencies

	@Inject
	ConsoleMetaManager consoleMetaManager;

	// prototype dependencies

	@Inject
	Provider<CoreAuthAction> authActionProvider;

	@Inject
	Provider<ConsoleFile> consoleFile;

	@Inject
	Provider<ConsoleContextPrivLookup> contextPrivLookupProvider;

	@Inject
	Provider<ConsoleContextTab> contextTab;

	@Inject
	Provider<MessageSetAction> messageSetActionProvider;

	@Inject
	Provider<MessageSetPart> messageSetPartProvider;

	@Inject
	Provider<SimpleMessageSetFinder> simpleMessageSetFinderProvider;

	@Inject
	Provider<TabContextResponder> tabContextResponderProvider;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer container;

	@BuilderSource
	ObjectSmsMessageSetPageSpec spec;

	@BuilderTarget
	ConsoleModuleImpl consoleModule;

	// state

	ConsoleHelper<?> consoleHelper;

	String privKey;
	String tabName;
	String tabLabel;
	String fileName;
	String responderName;

	BooleanLookup canViewLookup;
	BooleanLookup canUpdateLookup;
	MessageSetFinder messageSetFinder;

	// build meta

	public
	void buildMeta (
			ConsoleMetaModuleImpl consoleMetaModule) {

	}

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		setDefaults ();

		prepare ();

		for (ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
				: consoleMetaManager.resolveExtensionPoint (
					container.extensionPointName ())) {

			buildTab (
				resolvedExtensionPoint);

			buildFile (
				resolvedExtensionPoint);

		}

	}

	void prepare () {

		canViewLookup =
			contextPrivLookupProvider.get ()
				.addPrivKey (privKey);

		canUpdateLookup =
			contextPrivLookupProvider.get ()
				.addPrivKey (privKey);

		messageSetFinder =
			simpleMessageSetFinderProvider.get ()
				.objectLookup (consoleHelper)
				.code (spec.messageSetCode ());

	}

	void buildTab (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		consoleModule.addContextTab (
			"end",
			contextTab.get ()
				.name (tabName)
				.defaultLabel (tabLabel)
				.localFile (fileName)
				.privKeys (Collections.singletonList (privKey)),
			resolvedExtensionPoint.contextTypeNames ());

	}

	void buildFile (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		Provider<PagePart> partFactory =
			new Provider<PagePart> () {

			@Override
			public
			PagePart get () {

				return messageSetPartProvider.get ()
					.messageSetFinder (messageSetFinder);

			}

		};

		final Provider<Responder> responder =
			tabContextResponderProvider.get ()
				.tab (tabName)
				.title (tabLabel)
				.pagePartFactory (partFactory);

		Action getAction =
			new Action () {

			@Override
			public
			Responder handle () {

				Action action =
					authActionProvider.get ()
						.lookup (canViewLookup)
						.normalResponder (responder);

				return action.handle ();

			}

		};

		Action postAction =
			new Action () {

			@Override
			public
			Responder handle () {

				Action action =
					messageSetActionProvider.get ()
						.responder (responder)
						.messageSetFinder (messageSetFinder)
						.privLookup (canUpdateLookup);

				return action.handle ();

			}

		};

		consoleModule.addContextFile (
			fileName,
			consoleFile.get ()
				.getResponderName (responderName)
				.getAction (getAction)
				.postAction (postAction)
				.privName (privKey),
			resolvedExtensionPoint.contextTypeNames ());

	}

	void setDefaults () {

		consoleHelper =
			container.consoleHelper ();

		privKey =
			stringFormat (
				"%s.manage",
				consoleHelper.objectName ());

		tabName =
			stringFormat (
				"%s.%s",
				container.pathPrefix (),
				spec.name ());

		tabLabel =
			capitalise (
				camelToSpaces (
					spec.name ()));

		fileName =
			stringFormat (
				"%s.%s",
				container.pathPrefix (),
				spec.name ());

		responderName =
			stringFormat (
				"%s%sResponder",
				container.newBeanNamePrefix (),
				capitalise (
					spec.name ()));

	}

}
