package wbs.clients.apn.chat.contact.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;
import javax.servlet.ServletException;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;

import wbs.clients.apn.chat.contact.model.ChatContactNoteObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatContactNoteRec;
import wbs.clients.apn.chat.contact.model.ChatMonitorInboxObjectHelper;
import wbs.clients.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.framework.web.Responder;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

@Log4j
@PrototypeComponent ("chatMonitorInboxUpdateNoteAction")
public
class ChatMonitorInboxUpdateNoteAction
	extends ConsoleAction {

	@Inject
	ChatContactNoteObjectHelper chatContactNoteHelper;

	@Inject
	ChatMonitorInboxObjectHelper chatMonitorInboxHelper;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	ObjectManager objectManager;

	@Inject
	UserObjectHelper userHelper;

	@Override
	public Responder backupResponder () {

		return responder ("chatMonitorInboxSummaryResponder");

	}

	@Override
	protected
	Responder goReal ()
		throws ServletException {

		String id =
			requestContext.parameter ("id");

		if (id == null)
			throw new RuntimeException ();

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		ChatMonitorInboxRec chatMonitorInbox =
			chatMonitorInboxHelper.find (
				requestContext.stuffInt ("chatMonitorInboxId"));

		ChatUserRec monitorChatUser =
			chatMonitorInbox.getMonitorChatUser ();

		ChatContactNoteRec note =
			chatContactNoteHelper.find (
				Integer.parseInt (id));

		if (requestContext.getForm ("deleteNote") != null) {

			log.info (
				stringFormat (
					"deleting note from %s",
					monitorChatUser.getName ()));

			objectManager.remove (
				note);

			transaction.commit ();

			requestContext.addNotice ("Note deleted");

		} else if (requestContext.getForm ("pegNote") != null) {

			note.setPegged (true);

			transaction.commit ();

			log.info (
				stringFormat (
					"User %s pegged chat user contact note %s",
					myUser.getId (),
					note.getId ()));

			requestContext.addNotice ("Note pegged");

		} else if (requestContext.getForm ("unpegNote") != null) {

			note.setPegged (false);

			transaction.commit ();

			log.info (
				stringFormat (
					"User %s unpegged chat user contact note %s",
					myUser.getId (),
					note.getId ()));

			requestContext.addNotice ("Note unpegged");

		} else {

			throw new RuntimeException ();
		}

		return null;

	}

}
