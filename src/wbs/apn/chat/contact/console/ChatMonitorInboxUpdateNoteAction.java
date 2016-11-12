package wbs.apn.chat.contact.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;

import javax.servlet.ServletException;

import lombok.Cleanup;
import lombok.NonNull;

import wbs.apn.chat.contact.model.ChatContactNoteObjectHelper;
import wbs.apn.chat.contact.model.ChatContactNoteRec;
import wbs.apn.chat.contact.model.ChatMonitorInboxObjectHelper;
import wbs.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectManager;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;
import wbs.web.responder.Responder;

@PrototypeComponent ("chatMonitorInboxUpdateNoteAction")
public
class ChatMonitorInboxUpdateNoteAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ChatContactNoteObjectHelper chatContactNoteHelper;

	@SingletonDependency
	ChatMonitorInboxObjectHelper chatMonitorInboxHelper;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserObjectHelper userHelper;

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
			@NonNull TaskLogger taskLogger)
		throws ServletException {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatMonitorInboxUpdateNodeAction.goReal ()",
				this);

		ChatMonitorInboxRec chatMonitorInbox =
			chatMonitorInboxHelper.findRequired (
				requestContext.stuffInteger (
					"chatMonitorInboxId"));

		ChatUserRec monitorChatUser =
			chatMonitorInbox.getMonitorChatUser ();

		ChatContactNoteRec note =
			chatContactNoteHelper.findRequired (
				requestContext.parameterIntegerRequired (
					"id"));

		if (
			requestContext.formIsPresent (
				"deleteNote")
		) {

			taskLogger.noticeFormat (
				"deleting note from %s",
				monitorChatUser.getName ());

			objectManager.remove (
				note);

			transaction.commit ();

			requestContext.addNotice (
				"Note deleted");

		} else if (
			requestContext.formIsPresent (
				"pegNote")

		) {

			note.setPegged (true);

			transaction.commit ();

			taskLogger.noticeFormat (
				"User %s pegged chat user contact note %s",
				integerToDecimalString (
					userConsoleLogic.userIdRequired ()),
				integerToDecimalString (
					note.getId ()));

			requestContext.addNotice (
				"Note pegged");

		} else if (
			requestContext.formIsPresent (
				"unpegNote")
		) {

			note.setPegged (false);

			transaction.commit ();

			taskLogger.noticeFormat (
				"User %s unpegged chat user contact note %s",
				integerToDecimalString (
					userConsoleLogic.userIdRequired ()),
				integerToDecimalString (
					note.getId ()));

			requestContext.addNotice (
				"Note unpegged");

		} else {

			throw new RuntimeException ();

		}

		return null;

	}

}
