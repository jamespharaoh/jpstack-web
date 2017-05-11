package wbs.console.formaction;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.hyphenToSpaces;
import static wbs.utils.string.StringUtils.joinWithSpace;
import static wbs.utils.string.StringUtils.stringFormat;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.console.forms.context.FormContextBuilder;
import wbs.console.forms.context.FormContextManager;
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

@PrototypeComponent ("contextTabFormActionPageBuilder")
@ConsoleModuleBuilderHandler
@SuppressWarnings ({ "rawtypes", "unchecked" })
public
class ContextTabFormActionPageBuilder
	implements BuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@SingletonDependency
	ConsoleMetaManager consoleMetaManager;

	@SingletonDependency
	FormContextManager formContextManager;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <ConsoleFile> consoleFileProvider;

	@PrototypeDependency
	Provider <ConsoleFormActionAction> consoleFormActionActionProvider;

	@PrototypeDependency
	Provider <ConsoleFormActionPart> consoleFormActionPartProvider;

	@PrototypeDependency
	Provider <ConsoleContextTab> consoleContextTabProvider;

	@PrototypeDependency
	Provider <TabContextResponder> tabContextResponderProvider;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer container;

	@BuilderSource
	ContextTabFormActionPageSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	String name;
	String helperBeanName;
	String tabName;
	String tabLabel;
	String localFile;
	String responderName;
	String actionName;
	String title;
	String pagePartName;

	Provider <ConsoleFormActionHelper> formActionHelperProvider;

	FormContextBuilder <?> actionFormContextBuilder;
	FormContextBuilder <?> historyFormContextBuilder;

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

			initFormFields ();

			initFormActionHelper (
				taskLogger);

			buildPagePartFactory ();
			buildAction ();

			buildResponder (
				taskLogger);

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

		}

	}

	void initFormFields () {

		actionFormContextBuilder =
			formContextManager.formContextBuilderRequired (
				consoleModule.name (),
				ifNull (
					spec.actionFormContextName (),
					stringFormat (
						"%s-form",
						spec.name ())),
				Object.class);

		historyFormContextBuilder =
			formContextManager.formContextBuilderRequired (
				consoleModule.name (),
				ifNull (
					spec.historyFormContextName (),
					stringFormat (
						"%s-history",
						spec.name ())),
				Object.class);

	}

	void initFormActionHelper (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"initFormActionHelper");

		) {

			formActionHelperProvider =
				componentManager.getComponentProviderRequired (
					taskLogger,
					helperBeanName,
					ConsoleFormActionHelper.class);

		}

	}

	void buildTab (
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

	void buildPagePartFactory () {

		pagePartFactory =
			parentTaskLogger ->
				consoleFormActionPartProvider.get ()

			.name (
				"action")

			.heading (
				capitalise (
					joinWithSpace (
						hyphenToSpaces (
							consoleModule.name ()),
						spec.name ())))

			.helper (
				formActionHelperProvider.get ())

			.actionFormContextBuilder (
				actionFormContextBuilder)

			.helpText (
				spec.helpText ())

			.submitLabel (
				spec.submitLabel ())

			.localFile (
				"/" + localFile)

			.historyHeading (
				spec.historyHeading ())

			.historyFormContextBuilder (
				historyFormContextBuilder)

		;

	}

	void buildAction () {

		actionProvider =
			() -> consoleFormActionActionProvider.get ()

			.name (
				"action")

			.formContextBuilder (
				actionFormContextBuilder)

			.formActionHelper (
				formActionHelperProvider.get ())

			.responderName (
				responderName);

	}

	void buildFile (
			@NonNull ResolvedConsoleContextExtensionPoint
				resolvedExtensionPoint) {

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
			spec.name ();

		helperBeanName =
			stringFormat (
				"%s%sFormActionHelper",
				container.newBeanNamePrefix (),
				capitalise (
					ifNull (
						spec.helperName (),
						name)));

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
