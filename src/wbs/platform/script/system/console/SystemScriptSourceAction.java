package wbs.platform.script.system.console;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.script.core.logic.DiffLogic;
import wbs.platform.script.system.model.SystemScriptRec;
import wbs.platform.script.system.model.SystemScriptRevisionObjectHelper;
import wbs.platform.script.system.model.SystemScriptRevisionRec;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("systemScriptSourceAction")
public
class SystemScriptSourceAction
	extends ConsoleAction {

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	DiffLogic diffLogic;

	@Inject
	SystemScriptConsoleHelper systemScriptHelper;

	@Inject
	SystemScriptRevisionObjectHelper systemScriptRevisionHelper;

	@Inject
	UserObjectHelper userHelper;

	@Override
	public
	Responder backupResponder () {
		return responder ("systemScriptSourceResponder");
	}

	@Override
	public
	Responder goReal () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		SystemScriptRec script =
			systemScriptHelper.find (
				requestContext.stuffInt ("systemScriptId"));

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		// get params

		String newText =
			requestContext.parameter ("text");

		// check it actually changed

		if (script.getText ().equals (newText)) {

			requestContext.addNotice (
				"No changes made");

			return null;

		}

		// work out the diff

		String diff =
			diffLogic.unidiff (
				script.getText (),
				newText);

		// update the script

		script
			.setText (newText)
			.setRevision (script.getRevision () + 1);

		// create a new revision

		systemScriptRevisionHelper.insert (
			new SystemScriptRevisionRec ()
				.setSystemScript (script)
				.setUser (myUser)
				.setRevision (script.getRevision())
				.setDiff (diff));

		transaction.commit ();

		requestContext.addNotice (
			"System script text revision created");

		return null;

	}

}
