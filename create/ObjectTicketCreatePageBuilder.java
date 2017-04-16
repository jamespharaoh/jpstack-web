package wbs.services.ticket.create;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Collections;
import java.util.List;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.console.forms.FieldsProvider;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleBuilder;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.part.PagePart;
import wbs.console.part.PagePartFactory;
import wbs.console.responder.ConsoleFile;
import wbs.console.tab.ConsoleContextTab;
import wbs.console.tab.TabContextResponder;

import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderComponent;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.object.criteria.WhereDeletedCriteriaSpec;
import wbs.platform.object.criteria.WhereICanManageCriteriaSpec;
import wbs.platform.object.criteria.WhereNotDeletedCriteriaSpec;

import wbs.services.ticket.core.model.TicketManagerRec;
import wbs.services.ticket.core.model.TicketRec;
import wbs.web.action.Action;

@PrototypeComponent ("objectTicketCreatePageBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectTicketCreatePageBuilder
	implements BuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@SingletonDependency
	ConsoleModuleBuilder consoleModuleBuilder;

	@SingletonDependency
	ConsoleMetaManager consoleMetaManager;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <ConsoleFile> consoleFileProvider;

	@PrototypeDependency
	Provider <ConsoleContextTab> contextTabProvider;

	@PrototypeDependency
	Provider <ObjectTicketCreateSetFieldSpec> ticketCreateSetFieldSpecProvider;

	@PrototypeDependency
	Provider <ObjectTicketCreatePart <TicketRec, TicketManagerRec>>
	objectTicketCreatePartProvider;

	@PrototypeDependency
	Provider <ObjectTicketCreateAction <TicketRec, TicketManagerRec>>
	objectTicketCreateActionProvider;

	@PrototypeDependency
	Provider <TabContextResponder> tabContextResponderProvider;

	@PrototypeDependency
	Provider <WhereDeletedCriteriaSpec> whereDeletedCriteriaSpecProvider;

	@PrototypeDependency
	Provider <WhereICanManageCriteriaSpec> whereICanManageCriteriaSpecProvider;

	@PrototypeDependency
	Provider <WhereNotDeletedCriteriaSpec> whereNotDeletedCriteriaSpecProvider;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer <TicketRec> container;

	@BuilderSource
	ObjectTicketCreatePageSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	ConsoleHelper <TicketRec> consoleHelper;

	String typeCode;
	FieldsProvider <TicketRec, TicketManagerRec> fieldsProvider;
	List <ObjectTicketCreateSetFieldSpec> ticketFields;
	String name;
	String tabName;
	String tabLabel;
	String localFile;
	Boolean hideTab;
	String responderName;
	String targetContextTypeName;
	String targetResponderName;
	FormFieldSet <TicketRec> fields;
	String createTimeFieldName;
	String createUserFieldName;
	String createPrivDelegate;
	String createPrivCode;
	String privKey;
	String ticketManagerPath;

	// build

	@BuildMethod
	@Override
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder builder) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"build");

		setDefaults (
			taskLogger);

		for (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
				: consoleMetaManager.resolveExtensionPoint (
					container.extensionPointName ())
		) {

			buildTab (
				container.taskLogger (),
				resolvedExtensionPoint);

			buildFile (
				resolvedExtensionPoint);

		}

		buildResponder ();

	}

	void buildTab (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"buildTab");

		consoleModule.addContextTab (
			taskLogger,
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

		Provider <Action> createActionProvider =
			() -> objectTicketCreateActionProvider.get ()

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

			.fields (
				fields)

			.formFieldsProvider (
				fieldsProvider)

			.ticketFieldSpecs(
					ticketFields)

			.ticketManagerPath(
				ticketManagerPath)

			.createTimeFieldName (
				createTimeFieldName)

			.createUserFieldName (
				createUserFieldName);

			consoleModule.addContextFile (

				localFile,

				consoleFileProvider.get ()

					.getResponderName (
						responderName)

					.postActionProvider (
						createActionProvider)

					/*.privKeys (
						Collections.singletonList (privKey)*/,

				resolvedExtensionPoint.contextTypeNames ());

	}

	void buildResponder () {

		PagePartFactory partFactory =
			new PagePartFactory () {

			@Override
			public
			PagePart buildPagePart (
					@NonNull TaskLogger parentTaskLogger) {

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

	void setDefaults (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"setDefaults");

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

			fieldsProvider =
				genericCastUnchecked (
					componentManager.getComponentRequired (
						taskLogger,
						spec.fieldsProviderName (),
						FieldsProvider.class));

		}

		ticketFields =
			spec.ticketFields;

	}

}
