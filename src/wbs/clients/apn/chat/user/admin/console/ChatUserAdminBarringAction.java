package wbs.clients.apn.chat.user.admin.console;

import static wbs.framework.utils.etc.Misc.nullIfEmptyString;

import javax.inject.Inject;

import lombok.Cleanup;

import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
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

@PrototypeComponent ("chatUserAdminBarringAction")
public
class ChatUserAdminBarringAction
	extends ConsoleAction {

	@Inject
	ChatUserLogic chatUserLogic;

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

		return responder ("chatUserAdminBarringResponder");

	}

	// implementation

	@Override
	public
	Responder goReal () {

		if (! requestContext.canContext ("chat.userAdmin")) {

			requestContext.addError ("Access denied");

			return null;

		}

		// get stuff

		int chatUserId =
			requestContext.stuffInt ("chatUserId");

		// get params

		String barOn =
			nullIfEmptyString (
				requestContext.parameterOrNull ("bar_on"));

		String barOff =
			nullIfEmptyString (
				requestContext.parameterOrNull ("bar_off"));

		String reason =
			nullIfEmptyString (
				requestContext.parameterOrNull ("reason"));

		// check params

		if ((barOn == null && barOff == null) || reason == null) {

			requestContext.addError (
				"Please fill in the form properly");

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

		// do the work
		String eventType = null;

		if (barOn != null && barOn.equals("on")) {

			chatUserLogic.logoff (chatUser, true);

			chatUser.setBarred(true);

			eventType = "chat_user_barred";

		} else if (barOff != null && barOff.equals("on")) {

			chatUser.setBarred(false);

			eventType = "chat_user_unbarred";

		}

		// create an event

		if (eventType != null) {

			eventLogic.createEvent (
				eventType,
				userConsoleLogic.userRequired (),
				chatUser,
				reason);

		}

		transaction.commit ();

		// return

		requestContext.addNotice ("User updated");

		return null;

	}

}
