package wbs.console.formaction;

import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.NullUtils.anyIsNotNull;
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

import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.console.forms.core.ConsoleFormManager;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleBuilderComponent;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.part.PagePartFactory;
import wbs.console.responder.ConsoleFile;
import wbs.console.tab.ConsoleContextTab;
import wbs.console.tab.TabContextResponder;

import wbs.framework.builder.Builder;
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

import wbs.web.mvc.WebAction;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("contextTabFormActionsPageBuilder")
@SuppressWarnings ({ "rawtypes", "unchecked" })
public
class ContextTabFormActionsPageBuilder
	implements ConsoleModuleBuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@SingletonDependency
	ConsoleMetaManager consoleMetaManager;

	@SingletonDependency
	ConsoleFormManager formContextManager;

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
	String actionName;
	String title;

	Provider <WebResponder> responderProvider;
	List <ConsoleFormAction> formActions;

	PagePartFactory pagePartFactory;
	Provider <WebAction> actionProvider;

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

	private
	ConsoleFormAction initFormAction (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ConsoleFormActionSpec actionSpec) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"initFormAction");

		) {

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

				.actionFormContextBuilder (
					formContextManager.getFormTypeRequired (
						taskLogger,
						consoleModule.name (),
						ifNull (
							actionSpec.actionFormTypeName (),
							stringFormat (
								"%s-action",
								actionSpec.name ())),
						Object.class))

				.submitLabel (
					ifNull (
						actionSpec.submitLabel (),
						hyphenToSpaces (
							actionSpec.name ())))

				.historyHeading (
					actionSpec.historyHeading ())

				.historyFormContextBuilder (
					ifThenElse (
						anyIsNotNull (
							actionSpec.historyHeading (),
							actionSpec.historyFormTypeName ()),
						() -> formContextManager.getFormTypeRequired (
							taskLogger,
							consoleModule.name (),
							ifNull (
								actionSpec.historyFormTypeName (),
								stringFormat (
									"%s-history",
									actionSpec.name ())),
							Object.class),
						() -> null))

			;

		}

	}

	void buildTab (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ResolvedConsoleContextExtensionPoint extensionPoint) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildTab");

		) {

			consoleModule.addContextTab (
				taskLogger,
				container.tabLocation (),
				consoleContextTabProvider.get ()
					.name (tabName)
					.defaultLabel (tabLabel)
					.localFile (localFile),
				extensionPoint.contextTypeNames ());

		}

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

			.responderProvider (
				responderProvider);

	}

	void buildFile (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ResolvedConsoleContextExtensionPoint extensionPoint) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildFile");

		) {

			consoleModule.addContextFile (
				localFile,
				consoleFileProvider.get ()

					.getResponderProvider (
						responderProvider)

					.postActionProvider (
						actionProvider),

				extensionPoint.contextTypeNames ()
			);

		}

	}

	void buildResponder (
			@NonNull TaskLogger parentTaskLogger) {

		responderProvider =
			() -> tabContextResponderProvider.get ()

			.tab (
				tabName)

			.title (
				title)

			.pagePartFactory (
				pagePartFactory)

		;

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
