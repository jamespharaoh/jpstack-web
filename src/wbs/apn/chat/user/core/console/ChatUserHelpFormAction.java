package wbs.apn.chat.user.core.console;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.apn.chat.help.logic.ChatHelpLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.sms.gsm.Gsm;

@PrototypeComponent ("chatUserHelpFormAction")
public
class ChatUserHelpFormAction
	extends ConsoleAction {

	@Inject
	ChatHelpLogic chatHelpLogic;

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	UserObjectHelper userHelper;

	@Override
	public
	Responder backupResponder () {
		return responder ("chatUserHelpFormResponder");
	}

	@Override
	protected
	Responder goReal () {

		// get parameters

		String text =
			requestContext.parameter("text");

		// check parameters

		if (text.length() == 0) {
			requestContext.addError("Please type a message");
			return null;
		}

		if (!Gsm.isGsm(text)) {
			requestContext.addError("Reply contains invalid characters");
			return null;
		}

		if (Gsm.length(text) > 149) {
			requestContext.addError("Text is too long!");
			return null;
		}

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		// get objects

		UserRec user =
			userHelper.find (
				requestContext.userId ());

		ChatUserRec chatUser =
			chatUserHelper.find (
				requestContext.stuffInt ("chatUserId"));

		// send message

		chatHelpLogic.sendHelpMessage (
			user,
			chatUser,
			text,
			null,
			null);

		transaction.commit ();

		// return

		requestContext.addNotice ("Message sent");

		return null;

	}

}
