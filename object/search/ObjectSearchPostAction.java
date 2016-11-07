package wbs.platform.object.search;

import static wbs.utils.etc.Misc.getMethodRequired;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.ReflectionUtils.methodInvoke;
import static wbs.utils.etc.TypeUtils.classInstantiate;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringFormat;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

import javax.inject.Provider;
import javax.servlet.ServletException;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.action.ConsoleAction;
import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextType;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.forms.FormFieldSet;
import wbs.console.module.ConsoleManager;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.RedirectResponder;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.web.Responder;
import wbs.utils.etc.PropertyUtils;

@Accessors (fluent = true)
@PrototypeComponent ("objectSearchAction")
public
class ObjectSearchPostAction <
	ObjectType extends Record <ObjectType>,
	SearchType extends Serializable,
	ResultType
>
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ConsoleManager consoleManager;

	@SingletonDependency
	Database database;

	@SingletonDependency
	FormFieldLogic fieldsLogic;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <RedirectResponder> redirectResponderProvider;

	@PrototypeDependency
	Provider <ObjectSearchCsvResponder <ResultType>>
		objectSearchCsvResponderProvider;

	// properties

	@Getter @Setter
	ConsoleHelper <ObjectType> consoleHelper;

	@Getter @Setter
	Class <SearchType> searchClass;

	@Getter @Setter
	String searchDaoMethodName;

	@Getter @Setter
	String resultsDaoMethodName;

	@Getter @Setter
	String sessionKey;

	@Getter @Setter
	String parentIdKey;

	@Getter @Setter
	String parentIdName;

	@Getter @Setter
	FormFieldSet <SearchType> searchFormFieldSet;

	@Getter @Setter
	List <FormFieldSet <ResultType>> resultsFormFieldSets;

	@Getter @Setter
	String searchResponderName;

	@Getter @Setter
	String fileName;

	// details

	@Override
	protected
	Responder backupResponder () {
		return null;
	}

	// implementation

	@Override
	protected
	Responder goReal ()
		throws ServletException {

		// handle new/repeat search buttons

		if (
			optionalIsPresent (
				requestContext.parameter (
					"new-search"))
		) {

			requestContext.session (
				sessionKey + "Results",
				null);

			return redirectResponderProvider.get ()

				.targetUrl (
					requestContext.resolveLocalUrl (
						"/" + fileName));

		}

		if (
			optionalIsPresent (
				requestContext.parameter (
					"repeat-search"))
		) {

			requestContext.session (
				sessionKey + "Results",
				null);

		}

		if (
			optionalIsPresent (
				requestContext.parameter (
					"download-csv"))
		) {

			return objectSearchCsvResponderProvider.get ()

				.consoleHelper (
					consoleHelper)

				.formFieldSets (
					resultsFormFieldSets)

				.resultsDaoMethodName (
					resultsDaoMethodName)

				.sessionKey (
					sessionKey);

		}

		// perform search

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"ObjectSearchPostAction.goReal ()",
				this);

		SearchType search =
			genericCastUnchecked (
				requestContext.session (
					sessionKey + "Fields"));

		if (search == null) {

			search =
				classInstantiate (
					searchClass);

			requestContext.session (
				sessionKey + "Fields",
				search);

		}

		fieldsLogic.implicit (
			searchFormFieldSet,
			search);

		consoleHelper.consoleHooks ().applySearchFilter (
			search);

		if (
			parentIdKey != null
			|| parentIdName != null
		) {

			if (
				parentIdKey == null
				|| parentIdName == null
			) {
				throw new RuntimeException ();
			}

			Object parentId =
				requestContext.stuff (
					parentIdKey);

			if (parentId == null) {
				throw new RuntimeException ();
			}

			PropertyUtils.setProperty (
				search,
				parentIdName,
				parentId);

		}

		// update search details

		UpdateResultSet updateResultSet =
			fieldsLogic.update (
				requestContext,
				searchFormFieldSet,
				search,
				ImmutableMap.of (),
				"search");

		if (updateResultSet.errorCount () > 0) {

			fieldsLogic.reportErrors (
				requestContext,
				updateResultSet,
				"search");

			requestContext.request (
				"objectSearchUpdateResultSet",
				updateResultSet);

			return responder (
				searchResponderName);

		}

		// perform search

		List<Long> objectIds;

		if (
			isNotNull (
				searchDaoMethodName)
		) {

			Method method =
				getMethodRequired (
					consoleHelper.getClass (),
					searchDaoMethodName,
					ImmutableList.<Class<?>>of (
						searchClass));

			objectIds =
				genericCastUnchecked (
					methodInvoke (
						method,
						consoleHelper,
						search));

		} else {

			objectIds =
				consoleHelper.searchIds (
					search);

		}

		if (objectIds.isEmpty ()) {

			// no results

			requestContext.addError (
				"No results returned from search");

			return responder (
				searchResponderName);

		} else if (

			objectIds.size () == 1

			&& isNull (
				resultsDaoMethodName)

		) {

			// single result

			ConsoleContextType targetContextType =
				consoleManager.contextType (
					consoleHelper.objectName () + ":combo",
					true);

			Optional<ConsoleContext> targetContext =
				consoleManager.relatedContext (
					requestContext.consoleContext (),
					targetContextType);

			if (
				optionalIsPresent (
					targetContext)
			) {

				return redirectResponderProvider.get ()

					.targetUrl (
						requestContext.resolveContextUrl (
							stringFormat (
								"%s",
								targetContext.get ().pathPrefix (),
								"/%s",
								consoleHelper.getPathId (
									objectIds.get (
										0)))));

			} else {

				Record<?> object =
					consoleHelper.findRequired (
						objectIds.get (
							0));

				return redirectResponderProvider.get ()

					.targetUrl (
						requestContext.resolveLocalUrl (
							consoleHelper.getDefaultLocalPathGeneric (
								object)));


			}

		} else {

			// multiple results

			requestContext.session (
				sessionKey + "Results",
				(Serializable)
				objectIds);

			return redirectResponderProvider.get ()

				.targetUrl (
					requestContext.resolveLocalUrl (
						"/" + fileName));

		}

		/*
		return responder (
			stringFormat (
				"%sSearchResultsResponder",
				consoleHelper.objectName ()));
		*/

	}

}
