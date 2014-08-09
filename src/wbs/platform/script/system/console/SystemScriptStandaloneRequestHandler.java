package wbs.platform.script.system.console;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.RequestContext;
import wbs.platform.console.responder.ConsoleRequestHandler;
import wbs.platform.console.responder.ErrorResponder;
import wbs.platform.priv.console.PrivChecker;
import wbs.platform.pyp.console.Pyp;
import wbs.platform.script.system.logic.SystemScriptLogic;
import wbs.platform.script.system.model.SystemScriptRec;

@PrototypeComponent ("systemScriptStandaloneRequestHandler")
public
class SystemScriptStandaloneRequestHandler
	extends ConsoleRequestHandler {

	@Inject
	Database database;

	@Inject
	PrivChecker privChecker;

	@Inject
	Pyp pyp;

	@Inject
	RequestContext requestContext;

	@Inject
	SystemScriptConsoleHelper systemScriptHelper;

	@Inject
	SystemScriptLogic systemScriptLogic;

	@Inject
	Provider<ErrorResponder> errorPage;

	@Override
	public
	void handle ()
		throws IOException {

		@Cleanup
		Transaction transaction =
			database.beginReadOnly ();

		SystemScriptRec script =
			systemScriptHelper.find (
				requestContext.requestInt ("systemScriptId"));

		if (script == null)
			throw new RuntimeException ();

		if (! privChecker.can (script, "run")) {

			errorPage.get ()
				.title ("Access denied")
				.message ("You do not have permission to run this script.")
				.execute ();

			return;

		}

		if (! script.getStandalone ()) {

			errorPage.get ()
				.title ("Invalid operation")
				.message ("This script can not be run as a standalone script.")
				.execute ();

			return;

		}

		String originalSource =
			script.getText ();

		String expandedSource =
			systemScriptLogic.expand (originalSource);

		pyp.execute (
			expandedSource);

	}

}
