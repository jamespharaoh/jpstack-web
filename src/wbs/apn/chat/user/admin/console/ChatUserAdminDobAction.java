package wbs.apn.chat.user.admin.console;

import static wbs.utils.string.StringUtils.nullIfEmptyString;

import javax.inject.Provider;

import lombok.NonNull;

import org.joda.time.LocalDate;

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

import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;

import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("chatUserAdminDobAction")
public
class ChatUserAdminDobAction
	extends ConsoleAction {

	// singleton dependencies

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

	// prototype dependencies

	@PrototypeDependency
	@NamedDependency ("chatUserAdminDobResponder")
	Provider <WebResponder> dobResponderProvider;

	// details

	@Override
	public
	WebResponder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return dobResponderProvider.get ();

	}

	// implementation

	@Override
	public
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

			String dobString =
				nullIfEmptyString (
					requestContext.parameterRequired (
						"dob"));

			if (dobString == null) {

				requestContext.addError (
					"Please enter a date of birth");

				return null;

			}

			LocalDate dobLocalDate;

			try {

				dobLocalDate =
					LocalDate.parse (dobString);

			} catch (Exception exception) {

				requestContext.addError (
					"Invalid date");

				return null;

			}

			// lookup database stuff

			ChatUserRec chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

			// update chat user

			chatUser

				.setDob (
					dobLocalDate);

			// create event

			eventLogic.createEvent (
				transaction,
				"chat_user_dob",
				userConsoleLogic.userRequired (
					transaction),
				chatUser,
				dobString);

			transaction.commit ();

			requestContext.addNotice (
				"Chat user date of birth updated");

			return null;

		}

	}

}
