package wbs.clients.apn.chat.user.admin.console;

import static wbs.framework.utils.etc.Misc.nullIfEmptyString;

import javax.inject.Inject;

import lombok.Cleanup;

import org.joda.time.LocalDate;

import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;

@PrototypeComponent ("chatUserAdminDobAction")
public
class ChatUserAdminDobAction
	extends ConsoleAction {

	// dependencies

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	UserConsoleLogic userConsoleLogic;

	@Inject
	UserObjectHelper userHelper;

	// details

	@Override
	public
	Responder backupResponder () {
		return responder ("chatUserAdminDobResponder");
	}

	// implementation

	@Override
	public
	Responder goReal () {

		// get stuff

		int chatUserId =
			requestContext.stuffInt ("chatUserId");

		// get params

		String dobString =
			nullIfEmptyString (
				requestContext.parameterOrNull ("dob"));

		if (dobString == null) {

			requestContext.addError (
				"Please enter a date of birth");

			return null;

		}

		LocalDate dobLocalDate;

		try {

			dobLocalDate =
				LocalDate.parse (dobString);

		} catch (Exception exception) {

			requestContext.addError (
				"Invalid date");

			return null;

		}

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		// lookup database stuff

		ChatUserRec chatUser =
			chatUserHelper.find (
				chatUserId);

		// update chat user

		chatUser.setDob (dobLocalDate);

		// create event

		eventLogic.createEvent (
			"chat_user_dob",
			userConsoleLogic.userRequired (),
			chatUser,
			dobString);

		transaction.commit ();

		requestContext.addNotice (
			"Chat user date of birth updated");

		return null;

	}

}
