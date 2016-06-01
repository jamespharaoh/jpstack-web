package wbs.clients.apn.chat.contact.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;
import javax.servlet.ServletException;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;

import wbs.clients.apn.chat.contact.model.ChatMonitorInboxObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;

@Log4j
@PrototypeComponent ("chatMonitorInboxAddNoteAction")
public
class ChatMonitorInboxAddNoteAction
	extends ConsoleAction {

	@Inject
	ChatContactNoteConsoleHelper chatContactNoteHelper;

	@Inject
	ChatMonitorInboxObjectHelper chatMonitorInboxHelper;

	@Inject
	Database database;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	UserConsoleLogic userConsoleLogic;

	@Inject
	UserObjectHelper userHelper;

	@Override
	public
	Responder backupResponder () {

		return responder ("chatMonitorInboxSummaryResponder");

	}

	@Override
	protected
	Responder goReal ()
		throws ServletException {

		String newNote =
			requestContext.parameterOrNull ("moreNotes");

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		ChatMonitorInboxRec chatMonitorInbox =
			chatMonitorInboxHelper.find (
				requestContext.stuffInt ("chatMonitorInboxId"));

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
