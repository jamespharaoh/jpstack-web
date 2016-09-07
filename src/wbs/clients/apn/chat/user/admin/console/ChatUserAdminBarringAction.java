package wbs.clients.apn.chat.user.admin.console;

import static wbs.framework.utils.etc.Misc.shouldNeverHappen;
import static wbs.framework.utils.etc.Misc.stringTrim;
import static wbs.framework.utils.etc.StringUtils.stringIsEmpty;

import lombok.Cleanup;

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

@PrototypeComponent ("chatUserAdminBarringAction")
public
class ChatUserAdminBarringAction
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

		return responder (
			"chatUserAdminBarringResponder");

	}

	// implementation

	@Override
	public
	Responder goReal () {

		if (
			! requestContext.canContext (
				"chat.userAdmin")
		) {

			requestContext.addError (
				"Access denied");

			return null;

		}

		// get stuff

		Long chatUserId =
			requestContext.stuffInteger (
				"chatUserId");

		// get params

		Boolean barOn =
			requestContext.parameterIsOn (
				"bar_on");

		Boolean barOff =
			requestContext.parameterIsOn (
				"bar_off");

		String reason =
			stringTrim (
				requestContext.parameterRequired (
					"reason"));

		// check params

		if (

			(! barOn && ! barOff)

			|| stringIsEmpty (
				reason)

		) {

			requestContext.addError (
				"Please fill in the form properly");

			return null;

		}

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatUserAdminBarringAction.goReal ()",
				this);

		// lookup database stuff

		ChatUserRec chatUser =
			chatUserHelper.findRequired (
				chatUserId);

		// do the work

		String eventType = null;
		String notice = null;

		if (barOn) {

			chatUserLogic.logoff (
				chatUser,
				true);

			chatUser

				.setBarred (
					true);

			eventType =
				"chat_user_barred";

			notice =
				"Chat user barred";

		} else if (barOff) {

			chatUser

				.setBarred (
					false);

			eventType =
				"chat_user_unbarred";

			notice =
				"Chat user unbarred";

		} else {

			throw shouldNeverHappen ();

		}

		// create an event

		eventLogic.createEvent (
			eventType,
			userConsoleLogic.userRequired (),
			chatUser,
			reason);

		transaction.commit ();

		// return

		requestContext.addNotice (
			notice);

		return null;

	}

}
