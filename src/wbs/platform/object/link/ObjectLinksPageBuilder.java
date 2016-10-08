package wbs.platform.object.link;

import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import javax.inject.Provider;

import lombok.NonNull;

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
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.model.ModelField;
import wbs.framework.entity.record.Record;
import wbs.framework.web.Action;
import wbs.framework.web.Responder;

@PrototypeComponent ("objectLinksPageBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectLinksPageBuilder <
	ObjectType extends Record <ObjectType>,
	TargetType extends Record <TargetType>
> {

	// singleton dependencies

	@SingletonDependency
	ConsoleMetaManager consoleMetaManager;

	@SingletonDependency
	ConsoleModuleBuilder consoleModuleBuilder;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <ConsoleFile> consoleFile;

	@PrototypeDependency
	Provider <ConsoleContextTab> contextTab;

	@PrototypeDependency
	Provider <ObjectLinksPart <ObjectType, TargetType>> objectLinksPart;

	@PrototypeDependency
	Provider <ObjectLinksAction> objectLinksAction;

	@PrototypeDependency
	Provider <TabContextResponder> tabContextResponder;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer <ObjectType> container;

	@BuilderSource
	ObjectLinksPageSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	ConsoleHelper <ObjectType> consoleHelper;

	String name;
	String tabName;
	String tabLabel;
	String localFile;
	String privKey;
	String responderName;

	String pageTitle;
	ModelField linksField;
	ConsoleHelper <TargetType> targetConsoleHelper;
	ModelField targetLinksField;
	String addEventName;
	String removeEventName;
	ObjectLinksAction.EventOrder eventOrder;
	String updateSignalName;
	String targetUpdateSignalName;
	String successNotice;
	FormFieldSet <TargetType> targetFields;

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
			@NonNull ResolvedConsoleContextExtensionPoint extensionPoint) {

		consoleModule.addContextTab (
			container.taskLogger (),
			container.tabLocation (),
			contextTab.get ()
				.name (tabName)
				.defaultLabel (tabLabel)
				.localFile (localFile)
				.privKeys (privKey),
			extensionPoint.contextTypeNames ());

	}

	void buildFile (
			@NonNull ResolvedConsoleContextExtensionPoint extensionPoint) {

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
			extensionPoint.contextTypeNames ());

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
					.targetFields (targetFields)
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
			objectManager.findConsoleHelper (
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

		targetFields =
			consoleModule.formFieldSet (
				spec.fieldsName (),
				targetConsoleHelper.objectClass ());

	}

}
