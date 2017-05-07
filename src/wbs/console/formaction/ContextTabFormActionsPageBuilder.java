package wbs.console.formaction;

import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElse;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.OptionalUtils.optionalOrElseRequired;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.hyphenToCamel;
import static wbs.utils.string.StringUtils.hyphenToSpaces;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.console.forms.FormFieldSet;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleImplementation;
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
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.action.Action;

@PrototypeComponent ("contextTabFormActionsPageBuilder")
@ConsoleModuleBuilderHandler
@SuppressWarnings ({ "rawtypes", "unchecked" })
public
class ContextTabFormActionsPageBuilder
	implements BuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@SingletonDependency
	ConsoleMetaManager consoleMetaManager;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <ConsoleFile> consoleFileProvider;

	@PrototypeDependency
	Provider <ConsoleFormActionsAction> consoleFormActionsActionProvider;

	@PrototypeDependency
	Provider <ConsoleFormActionsPart> consoleFormActionsPartProvider;

	@PrototypeDependency
	Provider <ConsoleContextTab> consoleContextTabProvider;

	@PrototypeDependency
	Provider <TabContextResponder> tabContextResponderProvider;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer container;

	@BuilderSource
	ContextTabFormActionsPageSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	String name;
	String tabName;
	String tabLabel;
	String localFile;
	String responderName;
	String actionName;
	String title;

	List <ConsoleFormAction> formActions;

	PagePartFactory pagePartFactory;
	Provider <Action> actionProvider;

	// build

	@BuildMethod
	@Override
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder builder) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			setDefaults ();

			initFormActions (
				taskLogger);

			buildPagePartFactory (
				taskLogger);

			buildAction ();

			buildResponder (
				taskLogger);

			for (
				ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
					: consoleMetaManager.resolveExtensionPoint (
						container.extensionPointName ())
			) {

				buildTab (
					taskLogger,
					resolvedExtensionPoint);

				buildFile (
					taskLogger,
					resolvedExtensionPoint);

			}

		}

	}

	void initFormActions (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"initFormActions");

		) {

			formActions =
				iterableMapToList (
					spec.actions (),
					actionSpec ->
						initFormAction (
							parentTaskLogger,
							actionSpec));

		}

	}

	ConsoleFormAction initFormAction (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ConsoleFormActionSpec actionSpec) {

		String helperBeanName =
			stringFormat (
				"%s%sFormActionHelper",
				container.newBeanNamePrefix (),
				capitalise (
					hyphenToCamel (
						ifNull (
							actionSpec.name (),
							actionSpec.helperName ()))));

		ConsoleFormActionHelper <?, ?> actionHelper =
			genericCastUnchecked (
				componentManager.getComponentRequired (
					parentTaskLogger,
					helperBeanName,
					ConsoleFormActionHelper.class));

		return new ConsoleFormAction ()

			.name (
				actionSpec.name ())

			.helper (
				genericCastUnchecked (
					actionHelper))

			.heading (
				capitalise (
					hyphenToSpaces (
						actionSpec.name ())))

			.helpText (
				actionSpec.helpText ())

			.formFields (
				ifNotNullThenElse (
					actionSpec.fieldsName (),
					() -> consoleModule.formFieldSetRequired (
						actionSpec.fieldsName ()),
					() -> optionalOrElseRequired (
						consoleModule.formFieldSet (
							stringFormat (
								"%s-form",
								actionSpec.name ())),
						() -> new FormFieldSet ())))

			.submitLabel (
				ifNull (
					actionSpec.submitLabel (),
					hyphenToSpaces (
						actionSpec.name ())))

			.historyHeading (
				actionSpec.historyHeading ())

			.historyFields (
				ifNotNullThenElse (
					actionSpec.historyHeading (),
					() -> optionalOrNull (
						consoleModule.formFieldSet (
							ifNull (
								actionSpec.historyFieldsName (),
								stringFormat (
									"%s-history",
									actionSpec.name ())))),
					() -> (FormFieldSet <?>) null))

		;

	}

	void buildTab (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ResolvedConsoleContextExtensionPoint extensionPoint) {

		consoleModule.addContextTab (
			container.taskLogger (),
			container.tabLocation (),
			consoleContextTabProvider.get ()
				.name (tabName)
				.defaultLabel (tabLabel)
				.localFile (localFile),
			extensionPoint.contextTypeNames ());

	}

	void buildPagePartFactory (
			@NonNull TaskLogger parentTaskLogger) {

		pagePartFactory =
			nextTaskLogger ->
				consoleFormActionsPartProvider.get ()

			.formActions (
				genericCastUnchecked (
					formActions))

			.localFile (
				"/" + localFile);

	}

	void buildAction () {

		actionProvider =
			() -> consoleFormActionsActionProvider.get ()

			.formActions (
				genericCastUnchecked (
					formActions))

			.responderName (
				responderName);

	}

	void buildFile (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		consoleModule.addContextFile (
			localFile,
			consoleFileProvider.get ()

				.getResponderName (
					responderName)

				.postActionProvider (
					actionProvider),

			resolvedExtensionPoint.contextTypeNames ());

	}

	void buildResponder (
			@NonNull TaskLogger parentTaskLogger) {

		consoleModule.addResponder (
			responderName,
			tabContextResponderProvider.get ()

				.tab (
					tabName)

				.title (
					title)

				.pagePartFactory (
					pagePartFactory)

		);

	}

	// defaults

	void setDefaults () {

		name =
			ifNull (
				spec.name (),
				"actions");

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

		responderName =
			stringFormat (
				"%s%sFormResponder",
				container.newBeanNamePrefix (),
				capitalise (
					name));

		actionName =
			stringFormat (
				"%s%sFormAction",
				container.existingBeanNamePrefix (),
				capitalise (
					name));

		title =
			capitalise (
				stringFormat (
					"%s %s",
					container.friendlyName (),
					camelToSpaces (
						name)));

	}

}
