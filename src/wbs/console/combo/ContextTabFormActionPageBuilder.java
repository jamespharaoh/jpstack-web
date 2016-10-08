package wbs.console.combo;

import static wbs.utils.string.StringUtils.camelToSpaces;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import javax.inject.Provider;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.console.forms.FormFieldSet;
import wbs.console.module.ConsoleMetaManager;
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
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.web.Action;
import wbs.framework.web.Responder;

@Log4j
@PrototypeComponent ("contextTabFormActionPageBuilder")
@ConsoleModuleBuilderHandler
@SuppressWarnings ({ "rawtypes", "unchecked" })
public
class ContextTabFormActionPageBuilder {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@SingletonDependency
	ConsoleMetaManager consoleMetaManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <ConsoleFile> consoleFileProvider;

	@PrototypeDependency
	Provider <ContextFormActionAction> contextFormActionActionProvider;

	@PrototypeDependency
	Provider <ContextFormActionPart> contextFormActionPartProvider;

	@PrototypeDependency
	Provider <ConsoleContextTab> contextTabProvider;

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

	FormFieldSet formFields;
	Provider<ConsoleFormActionHelper> formActionHelperProvider;

	Provider<PagePart> pagePartFactory;
	Action action;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		setDefaults ();

		initFormFields ();
		initFormActionHelper ();

		buildPagePartFactory ();
		buildAction ();
		buildResponder ();

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

	void initFormFields () {

		formFields =
			consoleModule.formFieldSets ().get (
				spec.fieldsName ());

	}

	void initFormActionHelper () {

		formActionHelperProvider =
			componentManager.getComponentProviderRequired (
				log,
				helperBeanName,
				ConsoleFormActionHelper.class);

	}

	void buildTab (
			@NonNull ResolvedConsoleContextExtensionPoint extensionPoint) {

		consoleModule.addContextTab (
			container.taskLogger (),
			container.tabLocation (),
			contextTabProvider.get ()
				.name (tabName)
				.defaultLabel (tabLabel)
				.localFile (localFile),
			extensionPoint.contextTypeNames ());

	}

	void buildPagePartFactory () {

		pagePartFactory =
			new Provider<PagePart> () {

			@Override
			public
			PagePart get () {

				return contextFormActionPartProvider.get ()

					.formFields (
						formFields)

					.formActionHelper (
						formActionHelperProvider.get ())

					.helpText (
						spec.helpText ())

					.submitLabel (
						spec.submitLabel ())

					.localFile (
						"/" + localFile);

			}

		};

	}

	void buildAction () {

		action =
			new Action () {

			@Override
			public
			Responder handle () {

				Action action =
					contextFormActionActionProvider.get ()

					.fields (
						formFields)

					.formActionHelper (
						formActionHelperProvider.get ())

					.responderName (
						responderName);

				return action.handle ();

			}

		};

	}

	void buildFile (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		consoleModule.addContextFile (
			localFile,
			consoleFileProvider.get ()

				.getResponderName (
					responderName)

				.postAction (
					action),

			resolvedExtensionPoint.contextTypeNames ());

	}

	void buildResponder () {

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
					name));

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
