package wbs.services.ticket.create;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.web.Action;
import wbs.framework.web.Responder;
import wbs.platform.console.annotations.ConsoleModuleBuilderHandler;
import wbs.platform.console.context.ConsoleContextBuilderContainer;
import wbs.platform.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.platform.console.forms.FormFieldSet;
import wbs.platform.console.helper.ConsoleHelper;
import wbs.platform.console.module.ConsoleMetaManager;
import wbs.platform.console.module.ConsoleModuleBuilder;
import wbs.platform.console.module.ConsoleModuleImpl;
import wbs.platform.console.part.PagePart;
import wbs.platform.console.responder.ConsoleFile;
import wbs.platform.console.tab.ConsoleContextTab;
import wbs.platform.console.tab.TabContextResponder;
import wbs.platform.object.criteria.WhereDeletedCriteriaSpec;
import wbs.platform.object.criteria.WhereICanManageCriteriaSpec;
import wbs.platform.object.criteria.WhereNotDeletedCriteriaSpec;
import wbs.services.ticket.core.console.FieldsProvider;
import wbs.services.ticket.core.model.TicketManagerObjectHelper;

@PrototypeComponent ("objectTicketCreatePageBuilder")
@ConsoleModuleBuilderHandler
public class ObjectTicketCreatePageBuilder {

	// dependencies

	@Inject
	ApplicationContext applicationContext;

	@Inject
	ConsoleModuleBuilder consoleModuleBuilder;

	@Inject
	ConsoleMetaManager consoleMetaManager;

	// prototype dependencies

	@Inject
	Provider<ConsoleFile> consoleFile;

	@Inject
	Provider<ConsoleContextTab> contextTab;

	@Inject
	Provider<ObjectTicketCreateSetFieldSpec> ticketCreateSetFieldSpec;

	@Inject
	Provider<ObjectTicketCreatePart> objectTicketCreatePart;

	@Inject
	Provider<ObjectTicketCreateAction> objectTicketCreateAction;

	@Inject
	Provider<TicketManagerObjectHelper> ticketManagerHelper;

	@Inject
	Provider<TabContextResponder> tabContextResponder;

	@Inject
	Provider<WhereDeletedCriteriaSpec> whereDeletedCriteriaSpec;

	@Inject
	Provider<WhereICanManageCriteriaSpec> whereICanManageCriteriaSpec;

	@Inject
	Provider<WhereNotDeletedCriteriaSpec> whereNotDeletedCriteriaSpec;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer container;

	@BuilderSource
	ObjectTicketCreatePageSpec spec;

	@BuilderTarget
	ConsoleModuleImpl consoleModule;

	// state

	ConsoleHelper<?> consoleHelper;

	String typeCode;
	FieldsProvider fieldsProvider;
	List<ObjectTicketCreateSetFieldSpec> ticketFields;
	String name;
	String tabName;
	String tabLabel;
	String localFile;
	Boolean hideTab;
	String responderName;
	String targetContextTypeName;
	String targetResponderName;
	FormFieldSet formFieldSet;
	String createTimeFieldName;
	String createUserFieldName;
	String createPrivDelegate;
	String createPrivCode;
	String privKey;
	String ticketManagerPath;

	// build

	@BuildMethod
	public
	void build (
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

	void buildTab (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		consoleModule.addContextTab (
			container.tabLocation (),
			contextTab.get ()
				.name (tabName)
				.defaultLabel (tabLabel)
				.localFile (localFile),
			hideTab
				? Collections.<String>emptyList ()
				: resolvedExtensionPoint.contextTypeNames ());

	}

	void buildFile (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		Action createAction =
			new Action () {

			@Override
			public
			Responder handle () {

				return objectTicketCreateAction.get ()

					.consoleHelper (
						consoleHelper)

					.typeCode (
						typeCode)

					.responderName (
						responderName)

					.targetContextTypeName (
						targetContextTypeName)

					.targetResponderName (
						targetResponderName)

					.createPrivDelegate (
						createPrivDelegate)

					.createPrivCode (
						createPrivCode)

					.formFieldSet (
						formFieldSet)

					.formFieldsProvider (
						fieldsProvider)

					.ticketFieldSpecs(
							ticketFields)

					.ticketManagerPath(
						ticketManagerPath)

					.createTimeFieldName (
						createTimeFieldName)

					.createUserFieldName (
						createUserFieldName)

					.handle ();

				}

			};

			consoleModule.addContextFile (

				localFile,

				consoleFile.get ()

					.getResponderName (
						responderName)

					.postAction (
						createAction)

					/*.privKeys (
						Collections.singletonList (privKey)*/,

				resolvedExtensionPoint.contextTypeNames ());

	}

	void buildResponder () {

		Provider<PagePart> partFactory =
				new Provider<PagePart> () {

				@Override
				public
				PagePart get () {

					return objectTicketCreatePart.get ()

						.consoleHelper (
							consoleHelper)

						.localFile(
							localFile)

						.ticketFieldSpecs(
							ticketFields)

						.fieldsProvider(
							fieldsProvider)

						.ticketManagerPath(
							ticketManagerPath);

				}

			};

			consoleModule.addResponder (

				responderName,

				tabContextResponder.get ()

					.tab (
						tabName)

					.title (
						capitalise (
							consoleHelper.friendlyName () + " create"))

					.pagePartFactory (
						partFactory));
	}

	// defaults

	void setDefaults () {

		name =
			spec.name ();

		typeCode =
			spec.typeCode ();

		tabName =
			ifNull (
				spec.tabName (),
				stringFormat (
					"%s.create",
					container.pathPrefix ()));

		responderName =
			ifNull (
				spec.responderName (),
				stringFormat (
					"%sCreateResponder",
					container.newBeanNamePrefix ()));

		targetContextTypeName =
			"Ticket";

		targetResponderName =
			"TicketSettingsResponder";

		createPrivDelegate =
			spec.createPrivDelegate ();

		createPrivCode =
			"ticket.create";

		tabLabel =
			"Create ticket";

		localFile =
			ifNull (
				spec.localFile (),
				stringFormat (
					"%s.%s",
					container.pathPrefix (),
					name));

		hideTab =
			spec.hideTab ();

		consoleHelper =
				container.consoleHelper ();

		tabName =
			ifNull (
				spec.tabName (),
				stringFormat (
					"%s.create",
					container.pathPrefix ()));


		responderName =
			ifNull (
				spec.responderName (),
				stringFormat (
					"%sCreateResponder",
					container.newBeanNamePrefix ()));

		ticketManagerPath =
			spec.ticketManager();

		if (spec.fieldsProviderName() != null) {

			fieldsProvider =
				applicationContext.getBean (
					spec.fieldsProviderName (),
					FieldsProvider.class);

		}

		ticketFields =
			spec.ticketFields;

	}

}
