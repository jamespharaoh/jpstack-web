package wbs.apn.chat.contact.console;

import static wbs.utils.string.StringUtils.stringFormat;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;

import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.web.responder.Responder;

@Log4j
@PrototypeComponent ("chatMonitorInboxAddNoteAction")
public
class ChatMonitorInboxAddNoteAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ChatContactNoteConsoleHelper chatContactNoteHelper;

	@SingletonDependency
	ChatMonitorInboxConsoleHelper chatMonitorInboxHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserConsoleHelper userHelper;

	// details

	@Override
	public
	Responder backupResponder () {

		return responder (
			"chatMonitorInboxSummaryResponder");

	}

	@Override
	protected
	Responder goReal (
			@NonNull TaskLogger taskLogger) {

		String newNote =
			requestContext.parameterRequired (
				"moreNotes");

		try (

			Transaction transaction =
				database.beginReadWrite (
					"ChatMonitorInboxAddNoteAction.goReal ()",
					this);

		) {

			ChatMonitorInboxRec chatMonitorInbox =
				chatMonitorInboxHelper.findFromContextRequired ();

			ChatUserRec userChatUser =
				chatMonitorInbox.getUserChatUser ();

			ChatUserRec monitorChatUser =
				chatMonitorInbox.getMonitorChatUser ();

			ChatRec chat =
				userChatUser.getChat ();

			if (newNote != null) {

				log.info (
					stringFormat (
						"Adding note to %s",
						chatMonitorInbox.getMonitorChatUser ().getName ()));

				chatContactNoteHelper.insert (
					chatContactNoteHelper.createInstance ()

					.setChat (
						chat)

					.setUser (
						userChatUser)

					.setMonitor (
						monitorChatUser)

					.setNotes (
						newNote)

					.setTimestamp (
						transaction.now ())

					.setConsoleUser (
						userConsoleLogic.userRequired ()));

			}

			transaction.commit ();

			return null;

		}

	}

}
