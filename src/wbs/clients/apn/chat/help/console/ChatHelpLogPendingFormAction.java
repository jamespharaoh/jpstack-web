package wbs.clients.apn.chat.help.console;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.clients.apn.chat.help.logic.ChatHelpLogic;
import wbs.clients.apn.chat.help.model.ChatHelpLogRec;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.sms.gsm.Gsm;

@PrototypeComponent ("chatHelpLogPendingFormAction")
public
class ChatHelpLogPendingFormAction
	extends ConsoleAction {

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
	UserObjectHelper userHelper;

	@Override
	public
	Responder backupResponder () {
		return responder ("chatHelpPendingFormResponder");
	}

	@Override
	protected
	Responder goReal () {

		// get params

		String text =
			requestContext.parameter ("text");

		boolean ignore =
			requestContext.parameter ("ignore") != null;

		// check params

		if (! ignore) {

			if (text.length () == 0) {
				requestContext.addError ("Please type a message");
				return null;
			}

			if (! Gsm.isGsm (text)) {
				requestContext.addError ("Reply contains invalid characters");
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
				this);

		// load objects from database

		ChatHelpLogRec helpRequest =
			chatHelpLogHelper.find (
				requestContext.stuffInt ("chatHelpLogId"));

		ChatUserRec chatUser =
			helpRequest.getChatUser ();

		UserRec user =
			userHelper.find (
				requestContext.userId ());

		// send message

		if (! ignore) {

			chatHelpLogic.sendHelpMessage (
				user,
				chatUser,
				text,
				helpRequest.getMessage ().getThreadId (),
				helpRequest);

		}

		// unqueue the request

		queueLogic.processQueueItem (
			helpRequest.getQueueItem (),
			user);

		transaction.commit ();

		requestContext.addNotice (
			ignore
				? "Request ignored"
				: "Reply sent");

		return responder ("queueHomeResponder");

	}

}