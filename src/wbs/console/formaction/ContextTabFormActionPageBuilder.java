package wbs.console.formaction;

import static wbs.utils.etc.NullUtils.anyIsNotNull;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import lombok.NonNull;

import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.console.forms.core.ConsoleFormManager;
import wbs.console.forms.core.ConsoleFormType;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleBuilderComponent;
import wbs.console.module.ConsoleModuleImplementation;
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
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.mvc.WebAction;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("contextTabFormActionPageBuilder")
@SuppressWarnings ({ "rawtypes", "unchecked" })
public
class ContextTabFormActionPageBuilder
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
	ComponentProvider <ConsoleFile> consoleFileProvider;

	@PrototypeDependency
	ComponentProvider <ConsoleFormActionAction> consoleFormActionActionProvider;

	@PrototypeDependency
	ComponentProvider <ConsoleFormActionPart> consoleFormActionPartProvider;

	@PrototypeDependency
	ComponentProvider <ConsoleContextTab> consoleContextTabProvider;

	@PrototypeDependency
	ComponentProvider <TabContextResponder> tabContextResponderProvider;

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
	String actionName;
	String title;
	String pagePartName;

	ComponentProvider <WebResponder> responderProvider;
	ComponentProvider <ConsoleFormActionHelper> formActionHelperProvider;

	ConsoleFormType <?> actionFormType;
	ConsoleFormType <?> historyFormType;

	ComponentProvider <WebAction> actionProvider;

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

			setDefaults (
				taskLogger);

			initFormFields (
				taskLogger);

			initFormActionHelper (
				taskLogger);

			buildAction (
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

	private
	void initFormFields (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"initFormFields");

		) {

			actionFormType =
				formContextManager.getFormTypeRequired (
					taskLogger,
					consoleModule.name (),
					ifNull (
						spec.actionFormTypeName (),
						stringFormat (
							"%s-action",
							spec.name ())),
					Object.class);

			if (
				anyIsNotNull (
					spec.historyHeading (),
					spec.historyFormTypeName ())
			) {

				historyFormType =
					formContextManager.getFormTypeRequired (
						taskLogger,
						consoleModule.name (),
						ifNull (
							spec.historyFormTypeName (),
							stringFormat (
								"%s-history",
								spec.name ())),
						Object.class);

			}

		}

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
				consoleContextTabProvider.provide (
					taskLogger)

					.name (
						tabName)

					.defaultLabel (
						tabLabel)

					.localFile (
						localFile),

				extensionPoint.contextTypeNames ()
			);

		}

	}

	private
	void buildAction (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildAction");

		) {

			actionProvider =
				taskLoggerNested ->
					consoleFormActionActionProvider.provide (
						taskLoggerNested)

				.name (
					"action")

				.formContextBuilder (
					actionFormType)

				.formActionHelper (
					formActionHelperProvider.provide (
						taskLoggerNested))

				.responderProvider (
					responderProvider)

			;

		}

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
				consoleFileProvider.provide (
					taskLogger)

					.getResponderProvider (
						responderProvider)

					.postActionProvider (
						actionProvider),

				extensionPoint.contextTypeNames ()
			);

		}

	}

	// defaults

	void setDefaults (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setDefaults");

		) {

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

			responderProvider =
				componentManager.getComponentProviderRequired (
					taskLogger,
					stringFormat (
						"%s%sFormResponder",
						container.newBeanNamePrefix (),
						capitalise (
							name)),
					WebResponder.class);

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

}
