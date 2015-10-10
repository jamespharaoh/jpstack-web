package wbs.platform.object.search;

import static wbs.framework.utils.etc.Misc.capitalise;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.annotations.ConsoleModuleBuilderHandler;
import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.ConsoleHelper;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleBuilder;
import wbs.console.module.ConsoleModuleImpl;
import wbs.console.part.PagePart;
import wbs.console.responder.ConsoleFile;
import wbs.console.tab.ConsoleContextTab;
import wbs.console.tab.TabContextResponder;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.web.Action;
import wbs.framework.web.Responder;

@PrototypeComponent ("objectSearchPageBuilder")
@ConsoleModuleBuilderHandler
public
class ObjectSearchPageBuilder {

	// dependencies

	@Inject
	ConsoleMetaManager consoleMetaManager;

	@Inject
	ConsoleModuleBuilder consoleModuleBuilder;

	@Inject
	ConsoleObjectManager consoleObjectManager;

	// prototype dependencies

	@Inject
	Provider<ConsoleFile> consoleFile;

	@Inject
	Provider<ConsoleContextTab> contextTab;

	@Inject
	Provider<TabContextResponder> tabContextResponder;

	@Inject
	Provider<ObjectSearchGetAction> objectSearchGetAction;

	@Inject
	Provider<ObjectSearchPart> objectSearchPart;

	@Inject
	Provider<ObjectSearchPostAction> objectSearchPostAction;

	@Inject
	Provider<ObjectSearchResultsPart> objectSearchResultsPart;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer container;

	@BuilderSource
	ObjectSearchPageSpec spec;

	@BuilderTarget
	ConsoleModuleImpl consoleModule;

	// state

	ConsoleHelper<?> consoleHelper;

	Class<?> searchClass;
	FormFieldSet searchFormFieldSet;
	FormFieldSet resultsFormFieldset;
	String sessionKey;
	String privKey;
	String parentIdKey;
	String parentIdName;
	String tabName;
	String fileName;
	String searchResponderName;
	String searchResultsResponderName;
	Integer itemsPerPage;

	Action searchGetAction;
	Action searchPostAction;

	// build

	@BuildMethod
	public
	void buildConsoleModule (
			Builder builder) {

		setDefaults ();

		buildGetAction ();
		buildPostAction ();

		buildSearchResponder ();
		buildResultsResponder ();

		for (ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
				: consoleMetaManager.resolveExtensionPoint (
					container.extensionPointName ())) {

			buildContextTab (
				resolvedExtensionPoint);

			buildContextFile (
				resolvedExtensionPoint);

		}

	}

	void buildContextTab (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		consoleModule.addContextTab (

			"end",

			contextTab.get ()

				.name (
					tabName)

				.defaultLabel (
					"Search")

				.localFile (
					fileName)

				.privKeys (
					privKey != null
						? Collections.singletonList (privKey)
						: Collections.<String>emptyList ()),

			resolvedExtensionPoint.contextTypeNames ());

	}

	void buildGetAction () {

		searchGetAction =
			new Action () {

			@Override
			public
			Responder handle () {

				return objectSearchGetAction.get ()

					.searchResponderName (
						searchResponderName)

					.searchResultsResponderName (
						searchResultsResponderName)

					.sessionKey (
						sessionKey)

					.handle ();

			}

		};

	}

	void buildPostAction () {

		searchPostAction =
			new Action () {

			@Override
			public
			Responder handle () {

				return objectSearchPostAction.get ()

					.consoleHelper (
						consoleHelper)

					.searchClass (
						searchClass)

					.sessionKey (
						sessionKey)

					.parentIdKey (
						parentIdKey)

					.parentIdName (
						parentIdName)

					.formFieldSet (
						searchFormFieldSet)

					.searchResponderName (
						searchResponderName)

					.fileName (
						fileName)

					.handle ();

			}

		};

	}

	void buildContextFile (
			ResolvedConsoleContextExtensionPoint resolvedExtensionPoint) {

		consoleModule.addContextFile (

			fileName,

			consoleFile.get ()

				.getAction (
					searchGetAction)

				.postAction (
					searchPostAction)

				.privKeys (
					privKey != null
						? Collections.singletonList (privKey)
						: Collections.<String>emptyList ()),

			resolvedExtensionPoint.contextTypeNames ());

	}

	void buildSearchResponder () {

		Provider<PagePart> searchPartFactory =
			new Provider<PagePart> () {

			@Override
			public
			PagePart get () {

				return objectSearchPart.get ()

					.consoleHelper (
						consoleHelper)

					.searchClass (
						searchClass)

					.sessionKey (
						sessionKey)

					.formFieldSet (
						searchFormFieldSet);

			}

		};

		consoleModule.addResponder (

			searchResponderName,

			tabContextResponder.get ()

				.tab (
					tabName)

				.title (
					capitalise (
						stringFormat (
							"%s search",
							consoleHelper.friendlyName ())))

				.pagePartFactory (
					searchPartFactory)

		);

	}

	void buildResultsResponder () {

		// search results responder

		Provider<PagePart> searchResultsPartFactory =
			new Provider<PagePart> () {

			@Override
			public
			PagePart get () {

				return objectSearchResultsPart.get ()

					.consoleHelper (
						consoleHelper)

					.sessionKey (
						sessionKey)

					.formFieldSet (
						resultsFormFieldset)

					.itemsPerPage (
						itemsPerPage)

					.targetContextTypeName (
						consoleHelper.objectName () + "+")

				;

			}

		};

		consoleModule.addResponder (

			searchResultsResponderName,

			tabContextResponder.get ()

				.tab (
					tabName)

				.title (
					capitalise (
						stringFormat (
							"%s search results",
							consoleHelper.friendlyName ())))

				.pagePartFactory (
					searchResultsPartFactory));

	}

	void setDefaults () {

		consoleHelper =
			container.consoleHelper ();

		String searchClassName =
			stringFormat (
				"%s.%sRec$%sSearch",
				consoleHelper
					.objectClass ()
					.getPackage ()
					.getName (),
				capitalise (
					consoleHelper.objectName ()),
				capitalise (
					consoleHelper.objectName ()));

		try {

			searchClass =
				Class.forName (
					searchClassName);

		} catch (ClassNotFoundException exception) {

			// TODO change all to nested classes, remove the following

			String alternateSearchClassName =
				stringFormat (
					"%s.%sSearch",
					consoleHelper
						.objectClass ()
						.getPackage ()
						.getName (),
					capitalise (
						consoleHelper.objectName ()));

			try {

				searchClass =
					Class.forName (
						alternateSearchClassName);

			} catch (ClassNotFoundException secondException) {

				throw new RuntimeException (
					stringFormat (
						"Search class not found: %s",
						searchClassName));

			}

		}

		searchFormFieldSet =
			consoleModule.formFieldSets ().get (
				spec.searchFieldsName ());

		resultsFormFieldset =
			consoleModule.formFieldSets ().get (
				spec.resultsFieldsName ());

		privKey =
			spec.privKey ();

		parentIdKey =
			spec.parentIdKey ();

		parentIdName =
			spec.parentIdName ();

		sessionKey =
			stringFormat (
				"%s.search",
				container.pathPrefix ());

		tabName =
			ifNull (
				spec.tabName (),
				stringFormat (
					"%s.search",
					container.pathPrefix ()));

		fileName =
			ifNull (
				spec.fileName (),
				stringFormat (
					"%s.search",
					container.pathPrefix ()));

		searchResponderName =
			ifNull (
				spec.searchResponderName (),
				stringFormat (
					"%sSearchResponder",
					container.newBeanNamePrefix ()));

		searchResultsResponderName =
			ifNull (
				spec.searchResultsResponderName (),
				stringFormat (
					"%sSearchResultsResponder",
					container.newBeanNamePrefix ()));

		itemsPerPage =
			100;

	}

}
