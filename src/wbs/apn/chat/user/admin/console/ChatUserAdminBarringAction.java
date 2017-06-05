package wbs.apn.chat.user.admin.console;

import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.utils.etc.Misc.stringTrim;
import static wbs.utils.string.StringUtils.stringIsEmpty;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;

import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.web.responder.Responder;

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

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserObjectHelper userHelper;

	// details

	@Override
	public
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return responder (
			"chatUserAdminBarringResponder");

	}

	// implementation

	@Override
	public
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

			if (
				! requestContext.canContext (
					"chat.userAdmin")
			) {

				requestContext.addError (
					"Access denied");

				return null;

			}

			// get params

			Boolean barOn =
				requestContext.parameterOn (
					"bar_on");

			Boolean barOff =
				requestContext.parameterOn (
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

			// lookup database stuff

			ChatUserRec chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

			// do the work

			String eventType = null;
			String notice = null;

			if (barOn) {

				chatUserLogic.logoff (
					transaction,
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
				transaction,
				eventType,
				userConsoleLogic.userRequired (
					transaction),
				chatUser,
				reason);

			transaction.commit ();

			// return

			requestContext.addNotice (
				notice);

			return null;

		}

	}

}
