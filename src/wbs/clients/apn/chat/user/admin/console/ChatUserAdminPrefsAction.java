package wbs.clients.apn.chat.user.admin.console;

import static wbs.framework.utils.etc.LogicUtils.anyOf;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.StringUtils.nullIfEmptyString;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.clients.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.Gender;
import wbs.clients.apn.chat.user.core.model.Orient;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleLogic;

@PrototypeComponent ("chatUserAdminPrefsAction")
public
class ChatUserAdminPrefsAction
	extends ConsoleAction {

	// dependencies

	@Inject
	ChatUserObjectHelper chatUserHelper;

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	UserConsoleLogic userConsoleLogic;

	// details

	@Override
	public
	Responder backupResponder () {

		return responder (
			"chatUserAdminPrefsResponder");

	}

	// implementation

	@Override
	public
	Responder goReal () {

		// get stuff

		int chatUserId =
			requestContext.stuffInt (
				"chatUserId");

		// get params

		String genderParam =
			nullIfEmptyString (
				requestContext.parameterRequired (
					"gender"));

		String orientParam =
			nullIfEmptyString (
				requestContext.parameterRequired (
					"orient"));

		// check params

		if (genderParam == null || orientParam == null) {

			requestContext.addError (
				"Please select a gender and an orient");

			return null;

		}

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatUserAdminPrefsAction.goReal ()",
				this);

		// lookup database stuff

		ChatUserRec chatUser =
			chatUserHelper.findRequired (
				chatUserId);

		// check changes

		Gender oldGender =
			chatUser.getGender ();

		Gender newGender =
			Gender.valueOf (
				genderParam);

		Orient oldOrient =
			chatUser.getOrient ();

		Orient newOrient =
			Orient.valueOf (
				orientParam);

		if (anyOf (
		
			() -> notEqual (
				oldGender,
				newGender),

			() -> notEqual (
				oldOrient,
				newOrient)

		)) {

			chatUser

				.setGender (
					newGender)

				.setOrient (
					newOrient);

			eventLogic.createEvent (
				"chat_user_prefs",
				userConsoleLogic.userRequired (),
				chatUser,
				chatUser.getGender ().toString (),
				chatUser.getOrient ().toString ());

		}

		transaction.commit ();

		requestContext.addNotice (
			"Chat user prefs updated");

		return null;

	}

}
