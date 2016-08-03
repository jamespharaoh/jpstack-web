package wbs.clients.apn.chat.help.console;

import javax.inject.Inject;

import lombok.Cleanup;

import com.google.common.base.Optional;

import wbs.clients.apn.chat.help.logic.ChatHelpLogic;
import wbs.clients.apn.chat.help.model.ChatHelpLogRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import static wbs.framework.utils.etc.OptionalUtils.isPresent;
import wbs.framework.web.Responder;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;
import wbs.sms.gsm.GsmUtils;

@PrototypeComponent ("chatHelpLogPendingFormAction")
public
class ChatHelpLogPendingFormAction
	extends ConsoleAction {

	// dependencies

	@Inject
	ChatHelpLogConsoleHelper chatHelpLogHelper;

	@Inject
	ChatHelpLogic chatHelpLogic;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	QueueLogic queueLogic;

	@Inject
	Database database;

	@Inject
	UserConsoleLogic userConsoleLogic;

	@Inject
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
	Responder goReal () {

		// get params

		String text =
			requestContext.parameterRequired (
				"text");

		boolean ignore =
			isPresent (
				requestContext.parameter (
					"ignore"));

		// check params

		if (! ignore) {

			if (text.length () == 0) {

				requestContext.addError (
					"Please type a message");

				return null;

			}

			if (! GsmUtils.isValidGsm (text)) {

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
				requestContext.stuffInt (
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