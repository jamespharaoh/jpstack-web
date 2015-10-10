package wbs.platform.object.search;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletException;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import wbs.console.action.ConsoleAction;
import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextType;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.helper.ConsoleHelper;
import wbs.console.module.ConsoleManager;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.RedirectResponder;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
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

	// properties

	@Getter @Setter
	ConsoleHelper<?> consoleHelper;

	@Getter @Setter
	Class<?> searchClass;

	@Getter @Setter
	String sessionKey;

	@Getter @Setter
	String parentIdKey;

	@Getter @Setter
	String parentIdName;

	@Getter @Setter
	FormFieldSet formFieldSet;

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

		if (requestContext.parameter ("new-search") != null) {

			requestContext.session (
				sessionKey + "Results",
				null);

			return redirectResponderProvider.get ()

				.targetUrl (
					requestContext.resolveLocalUrl (
						"/" + fileName));

		}

		if (requestContext.parameter ("repeat-search") != null) {

			requestContext.session (
				sessionKey + "Results",
				null);

		}

		// perform search

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				this);

		Object search =
			requestContext.session (
				sessionKey + "Fields");

		if (search == null) {

			search =
				searchClass.newInstance ();

			requestContext.session (
				sessionKey + "Fields",
				search);

		}

		if (parentIdKey != null || parentIdName != null) {

			if (parentIdKey == null || parentIdName == null)
				throw new RuntimeException ();

			Object parentId =
				requestContext.stuff (
					parentIdKey);

			if (parentId == null)
				throw new RuntimeException ();

			BeanLogic.setProperty (
				search,
				parentIdName,
				parentId);

		}

		// update search details

		UpdateResultSet updateResultSet =
			fieldsLogic.update (
				formFieldSet,
				search);

		if (updateResultSet.errorCount () > 0) {

			fieldsLogic.reportErrors (
				updateResultSet);

			return responder (
				searchResponderName);

		}

		// perform search

		List<Integer> objectIds =
			consoleHelper.searchIds (
				search);

		if (objectIds.isEmpty ()) {

			// no results

			requestContext.addError (
				"No results returned from search");

			return responder (
				searchResponderName);

		} else if (objectIds.size () == 1) {

			// single result

			ConsoleContextType targetContextType =
				consoleManager.contextType (
					consoleHelper.objectName () + "+",
					true);

			ConsoleContext targetContext =
				consoleManager.relatedContext (
					requestContext.consoleContext (),
					targetContextType);

			return redirectResponderProvider.get ()

				.targetUrl (
					requestContext.resolveContextUrl (
						stringFormat (
							"%s",
							targetContext.pathPrefix (),
							"/%s",
							consoleHelper.getPathId (
								objectIds.get (0)))));

		} else {

			// multiple results

			requestContext.session (
				sessionKey + "Results",
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
