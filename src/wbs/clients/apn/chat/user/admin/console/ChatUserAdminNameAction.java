package wbs.clients.apn.chat.user.admin.console;

import static wbs.framework.utils.etc.Misc.toEnum;
import static wbs.framework.utils.etc.StringUtils.nullIfEmptyString;
import static wbs.framework.utils.etc.StringUtils.stringNotEqualSafe;

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
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;

@PrototypeComponent ("chatUserAdminNameAction")
public
class ChatUserAdminNameAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ChatUserNameObjectHelper chatUserNameHelper;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	Database database;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
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
				requestContext.parameterRequired (
					"editReason"));

		if (editReason == null) {

			requestContext.addError (
				"Please select a valid reason");

			return null;

		}

		String name =
			nullIfEmptyString (
				requestContext.parameterRequired (
					"name"));

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatUserAdminNameAction.goReal ()",
				this);

		ChatUserRec chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInteger (
					"chatUserId"));

		if (
			stringNotEqualSafe (
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
