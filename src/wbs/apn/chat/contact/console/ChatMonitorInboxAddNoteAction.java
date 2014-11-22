package wbs.apn.chat.contact.console;

import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;
import javax.servlet.ServletException;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import wbs.apn.chat.contact.model.ChatContactNoteRec;
import wbs.apn.chat.contact.model.ChatMonitorInboxObjectHelper;
import wbs.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

@Log4j
@PrototypeComponent ("chatMonitorInboxAddNoteAction")
public
class ChatMonitorInboxAddNoteAction
	extends ConsoleAction {

	@Inject
	ChatMonitorInboxObjectHelper chatMonitorInboxHelper;

	@Inject
	Database database;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	ConsoleRequestContext requestContext;

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
			requestContext.parameter ("moreNotes");

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

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

			objectManager.insert (
				new ChatContactNoteRec ()

				.setChat (
					chat)

				.setUser (
					userChatUser)

				.setMonitor (
					monitorChatUser)

				.setNotes (
					newNote)

				.setTimestamp (
					instantToDate (
						transaction.now ()))

				.setConsoleUser (
					myUser));

		}

		transaction.commit ();

		return null;

	}

}
