package wbs.apn.chat.contact.console;

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

import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.web.responder.Responder;

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

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserConsoleHelper userHelper;

	// details

	@Override
	public
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return responder (
			"chatMonitorInboxSummaryResponder");

	}

	@Override
	protected
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWriteWithoutParameters (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

			String newNote =
				requestContext.parameterRequired (
					"moreNotes");

			ChatMonitorInboxRec chatMonitorInbox =
				chatMonitorInboxHelper.findFromContextRequired (
					transaction);

			ChatUserRec userChatUser =
				chatMonitorInbox.getUserChatUser ();

			ChatUserRec monitorChatUser =
				chatMonitorInbox.getMonitorChatUser ();

			ChatRec chat =
				userChatUser.getChat ();

			if (newNote != null) {

				transaction.noticeFormat (
					"Adding note to %s",
					chatMonitorInbox.getMonitorChatUser ().getName ());

				chatContactNoteHelper.insert (
					transaction,
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
						userConsoleLogic.userRequired (
							transaction)));

			}

			transaction.commit ();

			return null;

		}

	}

}
