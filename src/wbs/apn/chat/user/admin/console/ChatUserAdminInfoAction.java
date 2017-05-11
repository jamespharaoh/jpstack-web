package wbs.apn.chat.user.admin.console;

import static wbs.framework.entity.record.IdObject.objectId;
import static wbs.utils.etc.Misc.toEnum;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;

import wbs.sms.command.model.CommandObjectHelper;

import wbs.apn.chat.contact.logic.ChatSendLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserEditReason;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.info.model.ChatUserInfoObjectHelper;
import wbs.apn.chat.user.info.model.ChatUserInfoRec;
import wbs.apn.chat.user.info.model.ChatUserInfoStatus;
import wbs.web.responder.Responder;

@PrototypeComponent ("chatUserAdminInfoAction")
public
class ChatUserAdminInfoAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ChatSendLogic chatSendLogic;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ChatUserInfoObjectHelper chatUserInfoHelper;

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserObjectHelper userHelper;

	// details

	@Override
	public
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return responder (
			"chatUserAdminInfoResponder");

	}

	// implementation

	@Override
	public
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWriteWithoutParameters (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

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
					requestContext.parameterRequired (
						"editReason"));

			if (editReason == null) {

				requestContext.addError (
					"Please select a valid reason");

				return null;

			}

			String newInfo =
				requestContext.parameterOrEmptyString (
					"info");

			// load database objects

			ChatUserRec chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

			ChatRec chat =
				chatUser.getChat ();

			TextRec newInfoText =
				newInfo != null
					? textHelper.findOrCreate (
						transaction,
						newInfo)
					: null;

			TextRec oldInfoText =
				chatUser.getInfoText ();

			if (newInfoText != oldInfoText) {

				ChatUserInfoRec chatUserInfo =
					chatUserInfoHelper.insert (
						transaction,
						chatUserInfoHelper.createInstance ()

					.setChatUser (
						chatUser)

					.setCreationTime (
						transaction.now ())

					.setOriginalText (
						oldInfoText)

					.setEditedText (
						newInfoText)

					.setStatus (
						ChatUserInfoStatus.console)

					.setModerator (
						userConsoleLogic.userRequired (
							transaction))

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
						textHelper.findOrCreateFormat (
							transaction,
							"Please reply with a message we can send out to ",
							"people to introduce you. Say where you are, ",
							"describe yourself and say what you are looking ",
							"for.");

					chatSendLogic.sendMessageMagic (
						transaction,
						chatUser,
						null,
						messageText,
						commandHelper.findByCodeRequired (
							transaction,
							chat,
							"magic"),
						serviceHelper.findByCodeRequired (
							transaction,
							chat,
							"system"),
						objectId (
							commandHelper.findByCodeRequired (
								transaction,
								chat,
								"join_info")));

				}

			}

			transaction.commit ();

			requestContext.addNotice (
				"User's info updated");

			requestContext.setEmptyFormData ();

			return null;

		}

	}

}
