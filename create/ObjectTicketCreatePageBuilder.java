package wbs.services.ticket.create;

import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.console.forms.FieldsProvider;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.ConsoleHelper;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleBuilder;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.part.PagePart;
import wbs.console.responder.ConsoleFile;
import wbs.console.tab.ConsoleContextTab;
import wbs.console.tab.TabContextResponder;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.record.Record;
import wbs.framework.web.Action;
import wbs.framework.web.Responder;
import wbs.platform.object.criteria.WhereDeletedCriteriaSpec;
import wbs.platform.object.criteria.WhereICanManageCriteriaSpec;
import wbs.platform.object.criteria.WhereNotDeletedCriteriaSpec;
import wbs.services.ticket.core.model.TicketManagerObjectHelper;
import wbs.services.ticket.core.model.TicketManagerRec;
import wbs.services.ticket.core.model.TicketRec;

@PrototypeComponent ("objectTicketCreatePageBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectTicketCreatePageBuilder<
	ObjectType extends Record<ObjectType>,
	ParentType extends Record<ParentType>
> {

	// dependencies

	@Inject
	ApplicationContext applicationContext;

	@Inject
	ConsoleModuleBuilder consoleModuleBuilder;

	@Inject
	ConsoleMetaManager consoleMetaManager;

	// prototype dependencies

	@Inject
	Provider<ConsoleFile> consoleFileProvider;

	@Inject
	Provider<ConsoleContextTab> contextTabProvider;

	@Inject
	Provider<ObjectTicketCreateSetFieldSpec> ticketCreateSetFieldSpecProvider;

	@Inject
	Provider<ObjectTicketCreatePart<ObjectType,ParentType>>
	objectTicketCreatePartProvider;

	@Inject
	Provider<ObjectTicketCreateAction<ObjectType,ParentType>>
	objectTicketCreateActionProvider;

	@Inject
	Provider<TabContextResponder> tabContextResponderProvider;

	Provider<WhereDeletedCriteriaSpec> whereDeletedCriteriaSpecProvider;

	@Inject
	Provider<WhereICanManageCriteriaSpec> whereICanManageCriteriaSpecProvider;

	@Inject
	Provider<WhereNotDeletedCriteriaSpec> whereNotDeletedCriteriaSpecProvider;

	// indirect dependencies

	@Inject
	Provider<TicketManagerObjectHelper> ticketManagerHelperProvider;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer<ObjectType> container;

	@BuilderSource
	ObjectTicketCreatePageSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	ConsoleHelper<ObjectType> consoleHelper;

	String typeCode;
	FieldsProvider<TicketRec,TicketManagerRec> fieldsProvider;
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
			container.tabLocation (),
			contextTabProvider.get ()
				.name (tabName)
				.defaultLabel (tabLabel)
				.localFile (localFile),
			hideTab
				? Collections.<String>emptyList ()
				: resolvedExtensionPoint.contextTypeNames ());

	}

	void buildFile (
			@NonNull ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		Action createAction =
			new Action () {

			@Override
			public
			Responder handle () {

				return objectTicketCreateActionProvider.get ()

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

				consoleFileProvider.get ()

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

				return objectTicketCreatePartProvider.get ()

					.consoleHelper (
						consoleHelper)

					.localFile(
						localFile )

					.ticketFieldSpecs (
						ticketFields)

					.fieldsProvider (
						fieldsProvider)

					.ticketManagerPath (
						ticketManagerPath);

			}

		};

		consoleModule.addResponder (

			responderName,

			tabContextResponderProvider.get ()

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
			spec.ticketManager ();

		if (spec.fieldsProviderName () != null) {

			@SuppressWarnings ("unchecked")
			FieldsProvider<TicketRec,TicketManagerRec> fieldsProviderTemp =
				(FieldsProvider<TicketRec,TicketManagerRec>)
				applicationContext.getComponentRequired (
					spec.fieldsProviderName (),
					FieldsProvider.class);

			fieldsProvider =
				fieldsProviderTemp;

		}

		ticketFields =
			spec.ticketFields;

	}

}
