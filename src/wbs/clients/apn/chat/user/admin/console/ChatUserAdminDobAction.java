package wbs.clients.apn.chat.user.admin.console;

import static wbs.framework.utils.etc.Misc.nullIfEmptyString;

import javax.inject.Inject;

import lombok.Cleanup;

import org.joda.time.LocalDate;

import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("chatUserAdminDobAction")
public
class ChatUserAdminDobAction
	extends ConsoleAction {

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	UserObjectHelper userHelper;

	@Override
	public
	Responder backupResponder () {
		return responder ("chatUserAdminDobResponder");
	}

	@Override
	public
	Responder goReal () {

		// get stuff

		int chatUserId =
			requestContext.stuffInt ("chatUserId");

		// get params

		String dobString =
			nullIfEmptyString (
				requestContext.parameter ("dob"));

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

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		ChatUserRec chatUser =
			chatUserHelper.find (
				chatUserId);

		// update chat user

		chatUser.setDob (dobLocalDate);

		// create event

		eventLogic.createEvent (
			"chat_user_dob",
			myUser,
			chatUser,
			dobString);

		transaction.commit ();

		requestContext.addNotice (
			"Chat user date of birth updated");

		return null;

	}

}
