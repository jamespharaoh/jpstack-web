package wbs.clients.apn.chat.user.admin.console;

import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.nullIfEmptyString;
import static wbs.framework.utils.etc.Misc.toEnum;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.clients.apn.chat.contact.logic.ChatSendLogic;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserEditReason;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.info.model.ChatUserInfoObjectHelper;
import wbs.clients.apn.chat.user.info.model.ChatUserInfoRec;
import wbs.clients.apn.chat.user.info.model.ChatUserInfoStatus;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.sms.command.model.CommandObjectHelper;

@PrototypeComponent ("chatUserAdminInfoAction")
public
class ChatUserAdminInfoAction
	extends ConsoleAction {

	// dependencies

	@Inject
	ChatSendLogic chatSendLogic;

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ChatUserInfoObjectHelper chatUserInfoHelper;

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	Database database;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	TextObjectHelper textHelper;

	@Inject
	UserObjectHelper userHelper;

	// details

	@Override
	public
	Responder backupResponder () {
		return responder ("chatUserAdminInfoResponder");
	}

	// implementation

	@Override
	public
	Responder goReal () {

		// check privs

		if (! requestContext.canContext (
				"chat.userAdmin")) {

			requestContext.addError (
				"Access denied");

			return null;

		}

		// get params

		ChatUserEditReason editReason =
			toEnum (
				ChatUserEditReason.class,
				requestContext.parameter ("editReason"));

		if (editReason == null) {

			requestContext.addError (
				"Please select a valid reason");

			return null;

		}

		String newInfo =
			nullIfEmptyString (
				requestContext.parameter ("info"));

		// transaction...

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		// load database objects

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		ChatUserRec chatUser =
			chatUserHelper.find (
				requestContext.stuffInt ("chatUserId"));

		ChatRec chat =
			chatUser.getChat ();

		TextRec newInfoText =
			newInfo != null
				? textHelper.findOrCreate (newInfo)
				: null;

		TextRec oldInfoText =
			chatUser.getInfoText ();

		if (newInfoText != oldInfoText) {

			ChatUserInfoRec chatUserInfo =
				chatUserInfoHelper.insert (
					new ChatUserInfoRec ()

				.setChatUser (
					chatUser)

				.setCreationTime (
					instantToDate (
						transaction.now ()))

				.setOriginalText (
					oldInfoText)

				.setEditedText (
					newInfoText)

				.setStatus (
					ChatUserInfoStatus.console)

				.setModerator (
					myUser)

				.setEditReason (
					editReason)

			);

			chatUser

				.setInfoText (
					newInfoText);

			chatUser.getChatUserInfos ().add (
				chatUserInfo);

			if (newInfoText == null) {

				// TODO use a template

				TextRec messageText =
					textHelper.findOrCreate (
						"Please reply with a message we can send out " +
						"to people to introduce you. Say where you " +
						"are, describe yourself and say what you are " +
						"looking for.");

				chatSendLogic.sendMessageMagic (
					chatUser,
					null,
					messageText,
					commandHelper.findByCode (chat, "magic"),
					serviceHelper.findByCode (chat, "system"),
					commandHelper.findByCode (chat, "join_info").getId ());

			}

		}

		transaction.commit ();

		requestContext.addNotice (
			"User's info updated");

		requestContext.setEmptyFormData ();

		return null;

	}

}
