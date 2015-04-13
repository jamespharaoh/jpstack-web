package wbs.clients.apn.chat.user.admin.console;

import static wbs.framework.utils.etc.Misc.toEnum;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.clients.apn.chat.bill.model.ChatUserCreditMode;
import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("chatUserAdminCreditModeAction")
public
class ChatUserAdminCreditModeAction
	extends ConsoleAction {

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
	UserObjectHelper userHelper;

	@Override
	public
	Responder backupResponder () {
		return responder ("chatUserAdminCreditModeResponder");
	}

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
				requestContext.parameter ("creditMode"));

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

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		// if it changed

		if (newCreditMode != oldCreditMode) {

			// update chat user

			chatUserLogic.creditModeChange (
				chatUser,
				newCreditMode);

			// and log event

			eventLogic.createEvent (
				"chat_user_credit_mode",
				myUser,
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
