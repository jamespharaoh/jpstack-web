package wbs.apn.chat.help.console;

import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import com.google.common.base.Optional;

import lombok.Cleanup;
import lombok.NonNull;

import wbs.apn.chat.help.logic.ChatHelpLogic;
import wbs.apn.chat.help.model.ChatHelpLogRec;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;
import wbs.framework.web.Responder;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;
import wbs.sms.gsm.GsmUtils;

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
	ConsoleRequestContext requestContext;

	@SingletonDependency
	QueueLogic queueLogic;

	@SingletonDependency
	Database database;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserObjectHelper userHelper;

	// details

	@Override
	public
	Responder backupResponder () {
		return responder ("chatHelpLogPendingFormResponder");
	}

	// implementation

	@Override
	protected
	Responder goReal (
			@NonNull TaskLogger taskLogger) {

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

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatHelpLogPendingFormAction.goReal ()",
				this);

		// load objects from database

		ChatHelpLogRec helpRequest =
			chatHelpLogHelper.findRequired (
				requestContext.stuffInteger (
					"chatHelpLogId"));

		ChatUserRec chatUser =
			helpRequest.getChatUser ();

		// send message

		if (! ignore) {

			chatHelpLogic.sendHelpMessage (
				userConsoleLogic.userRequired (),
				chatUser,
				text,
				Optional.of (
					helpRequest.getMessage ().getThreadId ()),
				Optional.of (
					helpRequest));

		}

		// unqueue the request

		queueLogic.processQueueItem (
			helpRequest.getQueueItem (),
			userConsoleLogic.userRequired ());

		transaction.commit ();

		requestContext.addNotice (
			ignore
				? "Request ignored"
				: "Reply sent");

		return responder ("queueHomeResponder");

	}

}