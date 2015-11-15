package wbs.platform.object.link;

import static wbs.framework.utils.etc.Misc.camelToSpaces;
import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.ConsoleHelper;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleBuilder;
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
import wbs.framework.entity.model.ModelField;
import wbs.framework.web.Action;
import wbs.framework.web.Responder;

@PrototypeComponent ("objectLinksPageBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectLinksPageBuilder {

	// dependencies

	@Inject
	ConsoleMetaManager consoleMetaManager;

	@Inject
	ConsoleModuleBuilder consoleModuleBuilder;

	@Inject
	ConsoleObjectManager objectManager;

	// prototype dependencies

	@Inject
	Provider<ConsoleFile> consoleFile;

	@Inject
	Provider<ConsoleContextTab> contextTab;

	@Inject
	Provider<ObjectLinksPart> objectLinksPart;

	@Inject
	Provider<ObjectLinksAction> objectLinksAction;

	@Inject
	Provider<TabContextResponder> tabContextResponder;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer container;

	@BuilderSource
	ObjectLinksPageSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	ConsoleHelper<?> consoleHelper;

	String name;
	String tabName;
	String tabLabel;
	String localFile;
	String privKey;
	String responderName;

	String pageTitle;
	ModelField linksField;
	ConsoleHelper<?> targetConsoleHelper;
	ModelField targetLinksField;
	String addEventName;
	String removeEventName;
	ObjectLinksAction.EventOrder eventOrder;
	String updateSignalName;
	String targetUpdateSignalName;
	String successNotice;
	FormFieldSet formFieldSet;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

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
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		consoleModule.addContextTab (
			container.tabLocation (),
			contextTab.get ()
				.name (tabName)
				.defaultLabel (tabLabel)
				.localFile (localFile)
				.privKeys (privKey),
			resolvedExtensionPoint.contextTypeNames ());

	}

	void buildFile (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		Action action =
			new Action () {

			@Override
			public
			Responder handle () {

				return objectLinksAction.get ()

					.responderName (
						responderName)

					.contextHelper (
						consoleHelper)

					.contextLinkField (
						linksField.name ())

					.targetHelper (
						targetConsoleHelper)

					.targetLinkField (
						targetLinksField.name ())

					.addEventName (
						addEventName)

					.removeEventName (
						removeEventName)

					.eventOrder (
						eventOrder)

					.contextUpdateSignalName (
						updateSignalName)

					.targetUpdateSignalName (
						targetUpdateSignalName)

					.successNotice (
						successNotice)

					.handle ();

			}

		};

		consoleModule.addContextFile (
			localFile,
			consoleFile.get ()
				.getResponderName (responderName)
				.postAction (action)
				.privName (privKey),
			resolvedExtensionPoint.contextTypeNames ());

	}

	void buildResponder () {

		Provider<PagePart> partFactory =
			new Provider<PagePart> () {

			@Override
			public
			PagePart get () {

				return objectLinksPart.get ()
					.consoleHelper (container.consoleHelper ())
					.contextLinksField (linksField.name ())
					.targetHelper (targetConsoleHelper)
					.formFieldSet (formFieldSet)
					.localFile (localFile);

			}

		};

		consoleModule.addResponder (
			responderName,
			tabContextResponder.get ()
				.tab (tabName)
				.title (pageTitle)
				.pagePartFactory (partFactory));

	}

	// defaults

	void setDefaults () {

		consoleHelper =
			container.consoleHelper ();

		// general stuff

		name =
			spec.name ();

		tabName =
			stringFormat (
				"%s.%s",
				container.pathPrefix (),
				name);

		tabLabel =
			capitalise (
				camelToSpaces (
					name));

		localFile =
			stringFormat (
				"%s.%s",
				container.pathPrefix (),
				name);

		privKey =
			stringFormat (
				"%s.manage",
				container.pathPrefix ());

		responderName =
			stringFormat (
				"%s%sResponder",
				container.newBeanNamePrefix (),
				capitalise (
					name));

		pageTitle =
			stringFormat (
				"%s %s",
				container.consoleHelper ().friendlyName (),
				camelToSpaces (
					name));

		linksField =
			container.consoleHelper ().field (
				spec.linksFieldName ());

		targetConsoleHelper =
			objectManager.getConsoleObjectHelper (
				(Class<?>)
				linksField.collectionValueType ());

		targetLinksField =
			targetConsoleHelper.field (
				spec.targetLinksFieldName ());

		addEventName =
			spec.addEventName ();

		removeEventName =
			spec.removeEventName ();

		eventOrder =
			spec.eventOrder ();

		updateSignalName =
			spec.updateSignalName ();

		targetUpdateSignalName =
			spec.targetUpdateSignalName ();

		successNotice =
			spec.successNotice ();

		formFieldSet =
			consoleModule.formFieldSets ().get (
				spec.fieldsName ());

	}

}
