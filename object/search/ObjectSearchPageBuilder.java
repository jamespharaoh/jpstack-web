package wbs.platform.object.search;

import static wbs.utils.etc.LogicUtils.ifNotNullThenElse;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.TypeUtils.classForName;
import static wbs.utils.etc.TypeUtils.classForNameRequired;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.hyphenToCamel;
import static wbs.utils.string.StringUtils.hyphenToCamelCapitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.console.forms.core.ConsoleFormManager;
import wbs.console.forms.core.ConsoleFormType;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
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
import wbs.framework.entity.record.IdObject;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.mvc.WebAction;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("objectSearchPageBuilder")
public
class ObjectSearchPageBuilder <
	ObjectType extends Record <ObjectType>,
	SearchType extends Serializable,
	ResultType extends IdObject
> implements ConsoleModuleBuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ComponentManager componentManager;

	@SingletonDependency
	ConsoleMetaManager consoleMetaManager;

	@SingletonDependency
	ConsoleFormManager formContextManager;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// prototype dependencies

	@PrototypeDependency
	Provider <ConsoleFile> consoleFile;

	@PrototypeDependency
	Provider <ConsoleContextTab> contextTab;

	@PrototypeDependency
	Provider <TabContextResponder> tabContextResponder;

	@PrototypeDependency
	Provider <ObjectSearchGetAction> objectSearchGetAction;

	@PrototypeDependency
	Provider <ObjectSearchPart <ObjectType, SearchType>> objectSearchPart;

	@PrototypeDependency
	Provider <
		ObjectSearchPostAction <
			ObjectType,
			SearchType,
			ResultType
		>
	> objectSearchPostAction;

	@PrototypeDependency
	Provider <ObjectSearchResultsPart <ObjectType, ResultType>>
		objectSearchResultsPartProvider;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer <ObjectType> container;

	@BuilderSource
	ObjectSearchPageSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	ConsoleHelper <ObjectType> consoleHelper;

	Class <SearchType> searchClass;
	Class <ResultType> resultClass;

	ConsoleFormType <SearchType> searchFormType;

	Map <String, ObjectSearchResultsMode <ResultType>> resultsModes;

	String name;
	String sessionKey;
	String privKey;
	String parentIdKey;
	String parentIdName;
	String tabName;
	String tabLabel;
	String fileName;

	Provider <WebResponder> searchResponderProvider;
	Provider <WebResponder> resultsResponderProvider;

	Provider <WebAction> searchGetActionProvider;
	Provider <WebAction> searchPostActionProvider;

	// build

	@Override
	@BuildMethod
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder <TaskLogger> builder) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildConsoleModule");

		) {

			setDefaults (
				taskLogger);

			buildGetAction ();
			buildPostAction ();

			for (
				ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
					: consoleMetaManager.resolveExtensionPoint (
						container.extensionPointName ())
			) {

				buildContextTab (
					taskLogger,
					resolvedExtensionPoint);

				buildContextFile (
					resolvedExtensionPoint);

			}

		}

	}

	void buildContextTab (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ResolvedConsoleContextExtensionPoint extensionPoint) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildContextTab");

		) {

			consoleModule.addContextTab (
				taskLogger,
				"end",

				contextTab.get ()

					.name (
						tabName)

					.defaultLabel (
						tabLabel)

					.localFile (
						fileName)

					.privKeys (
						privKey != null
							? Collections.singletonList (privKey)
							: Collections.<String>emptyList ()),

				extensionPoint.contextTypeNames ());

		}

	}

	void buildGetAction () {

		searchGetActionProvider =
			() -> objectSearchGetAction.get ()

			.searchResponderProvider (
				searchResponderProvider)

			.resultsResponderProvider (
				resultsResponderProvider)

			.sessionKey (
				sessionKey);

	}

	void buildPostAction () {

		searchPostActionProvider =
			() -> objectSearchPostAction.get ()

			.consoleHelper (
				consoleHelper)

			.searchClass (
				searchClass)

			.searchDaoMethodName (
				spec.searchDaoMethodName ())

			.resultsDaoMethodName (
				spec.resultsDaoMethodName ())

			.sessionKey (
				sessionKey)

			.parentIdKey (
				parentIdKey)

			.parentIdName (
				parentIdName)

			.searchFormType (
				searchFormType)

			.resultsModes (
				resultsModes)

			.searchResponderProvider (
				searchResponderProvider)

			.fileName (
				fileName);

	}

	void buildContextFile (
			@NonNull ResolvedConsoleContextExtensionPoint
				resolvedExtensionPoint) {

		consoleModule.addContextFile (

			fileName,

			consoleFile.get ()

				.getActionProvider (
					searchGetActionProvider)

				.postActionProvider (
					searchPostActionProvider)

				.privKeys (
					privKey != null
						? Collections.singletonList (privKey)
						: Collections.<String>emptyList ()),

			resolvedExtensionPoint.contextTypeNames ());

	}

	private
	void setDefaults (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setDefaults");

		) {

			name =
				ifNull (
					spec.name (),
					"search");

			if (
				isNotNull (
					spec.objectTypeName ())
			) {

				consoleHelper =
					genericCastUnchecked (
						objectManager.findConsoleHelperRequired (
							spec.objectTypeName ()));

			} else {

				consoleHelper =
					container.consoleHelper ();

			}

			String searchClassName =
				ifNull (
					spec.searchClassName (),
					stringFormat (
						"%s.%sSearch",
						consoleHelper
							.objectClass ()
							.getPackage ()
							.getName (),
						capitalise (
							consoleHelper.objectName ())));

			Optional <Class <?>> searchClassOptional =
				classForName (
					searchClassName);

			if (
				optionalIsNotPresent (
					searchClassOptional)
			) {

				throw new RuntimeException (
					stringFormat (
						"Search class not found: %s",
						searchClassName));

			}

			searchClass =
				genericCastUnchecked (
					searchClassOptional.get ());

			resultClass =
				genericCastUnchecked (
					ifNotNullThenElse (
						spec.resultsClassName (),
						() -> classForNameRequired (
							spec.resultsClassName ()),
						() -> consoleHelper.objectClass ()));

			privKey =
				spec.privKey ();

			parentIdKey =
				spec.parentIdKey ();

			parentIdName =
				spec.parentIdName ();

			sessionKey =
				stringFormat (
					"%s.%s",
					container.pathPrefix (),
					name);

			tabName =
				ifNull (
					spec.tabName (),
					stringFormat (
						"%s.%s",
						container.pathPrefix (),
						name));

			tabLabel =
				ifNull (
					spec.tabLabel (),
					"Search");

			fileName =
				ifNull (
					spec.fileName (),
					stringFormat (
						"%s.%s",
						container.pathPrefix (),
						name));

			searchResponderProvider =
				componentManager.getComponentProviderRequired (
					taskLogger,
					stringFormat (
						"%s%sResponder",
						container.newBeanNamePrefix (),
						capitalise (
							name)),
					WebResponder.class);

			resultsResponderProvider =
				componentManager.getComponentProviderRequired (
					taskLogger,
					stringFormat (
						"%s%sResultsResponder",
						container.newBeanNamePrefix (),
						capitalise (
							name)),
					WebResponder.class);

			searchFormType =
				genericCastUnchecked (
					componentManager.getComponentRequired (
						taskLogger,
						stringFormat (
							"%s%sFormType",
							hyphenToCamel (
								container.consoleModule ().name ()),
							hyphenToCamelCapitalise (
								spec.searchFormTypeName ())),
						ConsoleFormType.class));

		}

	}

}
