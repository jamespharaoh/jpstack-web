package wbs.clients.apn.chat.user.admin.console;

import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.toEnum;

import javax.inject.Inject;

import lombok.Cleanup;

import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserEditReason;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.info.model.ChatUserInfoStatus;
import wbs.clients.apn.chat.user.info.model.ChatUserNameObjectHelper;
import wbs.clients.apn.chat.user.info.model.ChatUserNameRec;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;

@PrototypeComponent ("chatUserAdminNameAction")
public
class ChatUserAdminNameAction
	extends ConsoleAction {

	// dependencies

	@Inject
	ChatUserNameObjectHelper chatUserNameHelper;

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	UserConsoleLogic userConsoleLogic;

	@Inject
	UserObjectHelper userHelper;

	// details

	@Override
	public
	Responder backupResponder () {

		return responder (
			"chatUserAdminNameResponder");

	}

	// implementation

	@Override
	public
	Responder goReal () {

		if (! requestContext.canContext (
				"chat.userAdmin")) {

			requestContext.addError (
				"Access denied");

			return null;

		}

		ChatUserEditReason editReason =
			toEnum (
				ChatUserEditReason.class,
				requestContext.parameterOrNull("editReason"));

		if (editReason == null) {
			requestContext.addError ("Please select a valid reason");
			return null;
		}

		String name =
			requestContext.parameterOrNull ("name");

		if (name.equals (""))
			name = null;

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		ChatUserRec chatUser =
			chatUserHelper.findOrNull (
				requestContext.stuffInt ("chatUserId"));

		if (
			notEqual (
				chatUser.getName (),
				name)
		) {

			ChatUserNameRec chatUserName =
				chatUserNameHelper.insert (
					chatUserNameHelper.createInstance ()

				.setChatUser (
					chatUser)

				.setCreationTime (
					transaction.now ())

				.setOriginalName (
					chatUser.getName ())

				.setEditedName (
					name)

				.setModerator (
					userConsoleLogic.userRequired ())

				.setStatus (
					ChatUserInfoStatus.console)

				.setEditReason (
					editReason)

			);

			chatUser.getChatUserNames ().add (
				chatUserName);

			chatUser

				.setName (
					name);

		}

		transaction.commit ();

		requestContext.addNotice (
			"Chat user name updated");

		requestContext.setEmptyFormData ();

		return null;

	}

}
