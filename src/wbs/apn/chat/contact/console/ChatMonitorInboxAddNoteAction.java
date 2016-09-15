package wbs.apn.chat.contact.console;

import static wbs.utils.string.StringUtils.stringFormat;

import javax.servlet.ServletException;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;

import wbs.apn.chat.contact.console.ChatContactNoteConsoleHelper;
import wbs.apn.chat.contact.model.ChatMonitorInboxObjectHelper;
import wbs.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
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

	// singleton dependencies

	@SingletonDependency
	ChatContactNoteConsoleHelper chatContactNoteHelper;

	@SingletonDependency
	ChatMonitorInboxObjectHelper chatMonitorInboxHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserObjectHelper userHelper;

	// details

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
			requestContext.parameterRequired ("moreNotes");

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatMonitorInboxAddNoteAction.goReal ()",
				this);

		ChatMonitorInboxRec chatMonitorInbox =
			chatMonitorInboxHelper.findRequired (
				requestContext.stuffInteger (
					"chatMonitorInboxId"));

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
