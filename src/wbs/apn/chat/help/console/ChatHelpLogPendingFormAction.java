package wbs.apn.chat.help.console;

import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;

import wbs.sms.gsm.GsmUtils;

import wbs.apn.chat.help.logic.ChatHelpLogic;
import wbs.apn.chat.help.model.ChatHelpLogRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("chatHelpLogPendingFormAction")
public
class ChatHelpLogPendingFormAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ChatHelpLogConsoleHelper chatHelpLogHelper;

	@SingletonDependency
	ChatHelpLogic chatHelpLogic;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	QueueLogic queueLogic;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserObjectHelper userHelper;

	// prototype dependencies

	@PrototypeDependency
	@NamedDependency ("chatHelpLogPendingFormResponder")
	Provider <WebResponder> pendingFormResponderProvider;

	@PrototypeDependency
	@NamedDependency ("queueHomeResponder")
	Provider <WebResponder> queueHomeResponderProvider;

	// details

	@Override
	public
	WebResponder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return pendingFormResponderProvider.get ();

	}

	// implementation

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

			// get params

			String text =
				requestContext.parameterRequired (
					"text");

			boolean ignore =
				optionalIsPresent (
					requestContext.parameter (
						"ignore"));

			// check params

			if (! ignore) {

				if (text.length () == 0) {

					requestContext.addError (
						"Please type a message");

					return null;

				}

				if (! GsmUtils.gsmStringIsValid (text)) {

					requestContext.addError (
						"Reply contains invalid characters");

					return null;

				}

				/*
				if (Gsm.length(text) > 149) {
					requestContext.addError("Text is too long!");
					return null;
				}
				*/

			}

			// load objects from database

			ChatHelpLogRec helpRequest =
				chatHelpLogHelper.findFromContextRequired (
					transaction);

			ChatUserRec chatUser =
				helpRequest.getChatUser ();

			// send message

			if (! ignore) {

				chatHelpLogic.sendHelpMessage (
					transaction,
					userConsoleLogic.userRequired (
						transaction),
					chatUser,
					text,
					optionalOf (
						helpRequest.getMessage ().getThreadId ()),
					optionalOf (
						helpRequest));

			}

			// unqueue the request

			queueLogic.processQueueItem (
				transaction,
				helpRequest.getQueueItem (),
				userConsoleLogic.userRequired (
					transaction));

			transaction.commit ();

			requestContext.addNotice (
				ignore
					? "Request ignored"
					: "Reply sent");

			return queueHomeResponderProvider.get ();

		}

	}

}