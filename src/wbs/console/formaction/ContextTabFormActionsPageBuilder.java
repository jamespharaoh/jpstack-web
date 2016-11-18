package wbs.console.formaction;

import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.etc.NullUtils.ifNull;
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
	Action action;

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

		setDefaults ();

		initFormActions (
			taskLogger);

		buildPagePartFactory (
			taskLogger);

		buildAction (
			taskLogger);

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

	void initFormActions (
			@NonNull TaskLogger parentTaskLogger) {

		formActions =
			iterableMapToList (
				actionSpec ->
					initFormAction (
						parentTaskLogger,
						actionSpec),
				spec.actions ());

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

		ConsoleFormActionHelper <?> actionHelper =
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

			.formFields (
				consoleModule.formFieldSetRequired (
					ifNull (
						actionSpec.fieldsName (),
						stringFormat (
							"%s-form",
							actionSpec.name ()))))

			.heading (
				capitalise (
					hyphenToSpaces (
						actionSpec.name ())))

			.helpText (
				actionSpec.helpText ())

			.submitLabel (
				ifNull (
					actionSpec.submitLabel (),
					hyphenToSpaces (
						actionSpec.name ())));


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

	void buildAction (
			@NonNull TaskLogger parentTaskLogger) {

		action =
			nextTaskLogger ->
				consoleFormActionsActionProvider.get ()

			.formActions (
				genericCastUnchecked (
					formActions))

			.responderName (
				responderName)

			.handle (
				nextTaskLogger);

	}

	void buildFile (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		consoleModule.addContextFile (
			localFile,
			consoleFileProvider.get ()

				.getResponderName (
					responderName)

				.postAction (
					action),

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
