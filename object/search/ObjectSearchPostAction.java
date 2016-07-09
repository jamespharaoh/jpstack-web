package wbs.platform.object.search;

import static wbs.framework.utils.etc.Misc.getMethodRequired;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.isPresent;
import static wbs.framework.utils.etc.Misc.methodInvoke;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletException;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import wbs.console.action.ConsoleAction;
import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextType;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.ConsoleHelper;
import wbs.console.module.ConsoleManager;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.RedirectResponder;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.BeanLogic;
import wbs.framework.web.Responder;

@Accessors (fluent = true)
@PrototypeComponent ("objectSearchAction")
public
class ObjectSearchPostAction
	extends ConsoleAction {

	// dependencies

	@Inject
	ConsoleManager consoleManager;

	@Inject
	Database database;

	@Inject
	FormFieldLogic fieldsLogic;

	@Inject
	ConsoleRequestContext requestContext;

	// prototype dependencies

	@Inject
	Provider<RedirectResponder> redirectResponderProvider;

	@Inject
	Provider<ObjectSearchCsvResponder> objectSearchCsvResponderProvider;

	// properties

	@Getter @Setter
	ConsoleHelper<?> consoleHelper;

	@Getter @Setter
	Class<?> searchClass;

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
	FormFieldSet searchFormFieldSet;

	@Getter @Setter
	List<FormFieldSet> resultsFormFieldSets;

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
	@SneakyThrows ({
		IllegalAccessException.class,
		InstantiationException.class
	})
	protected
	Responder goReal ()
		throws ServletException {

		// handle new/repeat search buttons

		if (
			isPresent (
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
			isPresent (
				requestContext.parameter (
					"repeat-search"))
		) {

			requestContext.session (
				sessionKey + "Results",
				null);

		}

		if (
			isPresent (
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
				this);

		Serializable search =
			requestContext.session (
				sessionKey + "Fields");

		if (search == null) {

			search =
				(Serializable)
				searchClass.newInstance ();

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

			BeanLogic.setProperty (
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

		List<Integer> objectIds;

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

			@SuppressWarnings ("unchecked")
			List<Integer> objectIdsTemp =
				(List<Integer>)
				methodInvoke (
					method,
					consoleHelper,
					ImmutableList.<Object>of (
						search));

			objectIds =
				objectIdsTemp;

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
				isPresent (
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
									objectIds.get (0)))));

			} else {

				Record<?> object =
					consoleHelper.findRequired (
						objectIds.get (
							0));

				return redirectResponderProvider.get ()

					.targetUrl (
						requestContext.resolveLocalUrl (
							consoleHelper.getDefaultLocalPath (
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
