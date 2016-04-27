package wbs.clients.apn.chat.user.admin.console;

import javax.inject.Inject;

import lombok.Cleanup;

import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.ChatUserType;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;

@PrototypeComponent ("chatUserAdminDeleteAction")
public
class ChatUserAdminDeleteAction
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

		return responder (
			"chatUserAdminDeleteResponder");

	}

	// implementation

	@Override
	protected
	Responder goReal () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		ChatUserRec chatUser =
			chatUserHelper.find (
				requestContext.stuffInt ("chatUserId"));

		if (chatUser.getType () != ChatUserType.user) {
			requestContext.addError ("User is a monitor");
			return null;
		}

		if (requestContext.parameter ("deleteUser") != null) {

			if (chatUser.getNumber () == null) {
				requestContext.addWarning ("User is already deleted");
				return null;
			}

			chatUser.setNumber (null);

			eventLogic.createEvent (
				"chat_user_delete",
				userConsoleLogic.userRequired (),
				chatUser);

			transaction.commit ();

			requestContext.addNotice ("User deleted");

			return null;
		}

		if (requestContext.parameter ("undeleteUser") != null) {

			if (chatUser.getNumber () != null) {
				requestContext.addWarning ("User is not deleted");
				return null;
			}

			if (chatUser.getOldNumber () == null) {
				requestContext.addError ("Cannot undelete this user");
				return null;
			}

			ChatUserRec newChatUser =
				chatUserHelper.find (
					chatUser.getChat (),
					chatUser.getOldNumber ());

			if (newChatUser != null) {

				requestContext.addError ("Cannot undelete this user");

				return null;

			}

			chatUser
				.setNumber (chatUser.getOldNumber ());

			eventLogic.createEvent (
				"chat_user_undelete",
				userConsoleLogic.userRequired (),
				chatUser);

			transaction.commit ();

			requestContext.addNotice ("User undeleted");

			return null;

		}

		throw new RuntimeException ();

	}

}