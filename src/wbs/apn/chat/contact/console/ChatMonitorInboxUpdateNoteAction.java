package wbs.apn.chat.contact.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectManager;

import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;

import wbs.apn.chat.contact.model.ChatContactNoteRec;
import wbs.apn.chat.contact.model.ChatMonitorInboxRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("chatMonitorInboxUpdateNoteAction")
public
class ChatMonitorInboxUpdateNoteAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ChatContactNoteConsoleHelper chatContactNoteHelper;

	@SingletonDependency
	ChatMonitorInboxConsoleHelper chatMonitorInboxHelper;

	@ClassSingletonDependency
	LogContext logContext;

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

	// prototype dependencies

	@PrototypeDependency
	@NamedDependency ("chatMonitorInboxSummaryResponder")
	ComponentProvider <WebResponder> summaryResponderProvider;

	// details

	@Override
	public
	WebResponder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"backupResponder");

		) {

			return summaryResponderProvider.provide (
				taskLogger);

		}

	}

	@Override
	protected
	WebResponder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

			ChatMonitorInboxRec chatMonitorInbox =
				chatMonitorInboxHelper.findFromContextRequired (
					transaction);

			ChatUserRec monitorChatUser =
				chatMonitorInbox.getMonitorChatUser ();

			ChatContactNoteRec note =
				chatContactNoteHelper.findRequired (
					transaction,
					requestContext.parameterIntegerRequired (
						"id"));

			if (
				requestContext.formIsPresent (
					"deleteNote")
			) {

				transaction.noticeFormat (
					"deleting note from %s",
					monitorChatUser.getName ());

				objectManager.remove (
					transaction,
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

				transaction.noticeFormat (
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

				transaction.noticeFormat (
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

}
