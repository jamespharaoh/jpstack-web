package wbs.console.combo;

import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.camelToSpaces;
import static wbs.framework.utils.etc.StringUtils.capitalise;

import javax.inject.Inject;
import javax.inject.Provider;

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
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.web.Action;
import wbs.framework.web.Responder;

@PrototypeComponent ("contextTabFormActionPageBuilder")
@ConsoleModuleBuilderHandler
@SuppressWarnings ({ "rawtypes", "unchecked" })
public
class ContextTabFormActionPageBuilder {

	// dependencies

	@Inject
	ApplicationContext applicationContext;

	@Inject
	ConsoleMetaManager consoleMetaManager;

	// prototype dependencies

	@Inject
	Provider<ConsoleFile> consoleFile;

	@Inject
	Provider<ContextFormActionAction> contextFormActionActionProvider;

	@Inject
	Provider<ContextFormActionPart> contextFormActionPartProvider;

	@Inject
	Provider<ConsoleContextTab> contextTab;

	@Inject
	Provider<TabContextResponder> tabContextResponder;

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
			applicationContext.getComponentProviderRequired (
				helperBeanName,
				ConsoleFormActionHelper.class);

	}

	void buildTab (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		consoleModule.addContextTab (
			container.tabLocation (),
			contextTab.get ()
				.name (tabName)
				.defaultLabel (tabLabel)
				.localFile (localFile),
			resolvedExtensionPoint.contextTypeNames ());

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
			consoleFile.get ()

				.getResponderName (
					responderName)

				.postAction (
					action),

			resolvedExtensionPoint.contextTypeNames ());

	}

	void buildResponder () {

		consoleModule.addResponder (
			responderName,
			tabContextResponder.get ()

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
