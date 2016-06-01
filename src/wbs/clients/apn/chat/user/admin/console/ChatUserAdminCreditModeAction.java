package wbs.clients.apn.chat.user.admin.console;

import static wbs.framework.utils.etc.Misc.toEnum;

import javax.inject.Inject;

import lombok.Cleanup;

import wbs.clients.apn.chat.bill.model.ChatUserCreditMode;
import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;

@PrototypeComponent ("chatUserAdminCreditModeAction")
public
class ChatUserAdminCreditModeAction
	extends ConsoleAction {

	// dependencies

	@Inject
	ChatUserLogic chatUserLogic;

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	UserConsoleLogic userConsoleLogic;

	@Inject
	UserObjectHelper userHelper;

	// details

	@Override
	public
	Responder backupResponder () {
		return responder ("chatUserAdminCreditModeResponder");
	}

	// implementation

	@Override
	public
	Responder goReal () {

		// check privs

		if (! requestContext.canContext (
				"chat.userCredit")) {

			requestContext.addError (
				"Access denied");

			return null;

		}

		// get params

		ChatUserCreditMode newCreditMode =
			toEnum (
				ChatUserCreditMode.class,
				requestContext.parameterOrNull ("creditMode"));

		if (newCreditMode == null) {

			requestContext.addError (
				"Please select a valid credit mode");

			return null;

		}

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		ChatUserRec chatUser =
			chatUserHelper.find (
				requestContext.stuffInt ("chatUserId"));

		ChatUserCreditMode oldCreditMode =
			chatUser.getCreditMode ();

		// if it changed

		if (newCreditMode != oldCreditMode) {

			// update chat user

			chatUserLogic.creditModeChange (
				chatUser,
				newCreditMode);

			// and log event

			eventLogic.createEvent (
				"chat_user_credit_mode",
				userConsoleLogic.userRequired (),
				chatUser,
				oldCreditMode.toString (),
				newCreditMode.toString ());

		}

		transaction.commit ();

		// we're done

		requestContext.addNotice ("Credit mode updated");

		return null;

	}

}
