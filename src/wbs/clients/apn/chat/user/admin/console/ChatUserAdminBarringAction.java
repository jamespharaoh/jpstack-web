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
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

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
	UserObjectHelper userHelper;

	@Override
	public
	Responder backupResponder () {

		return responder ("chatUserAdminBarringResponder");

	}

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
				requestContext.parameter ("bar_on"));

		String barOff =
			nullIfEmptyString (
				requestContext.parameter ("bar_off"));

		String reason =
			nullIfEmptyString (
				requestContext.parameter ("reason"));

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

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

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
				myUser,
				chatUser,
				reason);

		}

		transaction.commit ();

		// return

		requestContext.addNotice ("User updated");

		return null;

	}

}
