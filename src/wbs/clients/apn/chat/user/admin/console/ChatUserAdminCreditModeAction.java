package wbs.clients.apn.chat.user.admin.console;

import static wbs.framework.utils.etc.Misc.toEnum;

import lombok.Cleanup;

import wbs.clients.apn.chat.bill.model.ChatUserCreditMode;
import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.logic.ChatUserLogic;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
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

	// singleton dependencies

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
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
				requestContext.parameterRequired (
					"creditMode"));

		if (newCreditMode == null) {

			requestContext.addError (
				"Please select a valid credit mode");

			return null;

		}

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatUserAdminCreditModeAction.goReal ()",
				this);

		ChatUserRec chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInteger (
					"chatUserId"));

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
