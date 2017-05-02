package wbs.apn.chat.user.admin.console;

import static wbs.utils.etc.Misc.toEnum;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

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

import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;

import wbs.apn.chat.date.logic.ChatDateLogic;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserDateMode;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.web.responder.Responder;

@PrototypeComponent ("chatUserAdminDateAction")
public
class ChatUserAdminDateAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ChatDateLogic chatDateLogic;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

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
			"chatUserAdminDateResponder");

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

			ChatUserDateMode dateMode =
				toEnum (
					ChatUserDateMode.class,
					requestContext.parameterRequired (
						"dateMode"));

			Long radius =
				parseIntegerRequired (
					requestContext.parameterRequired (
						"radius"));

			Long startHour =
				parseIntegerRequired (
					requestContext.parameterRequired (
						"startHour"));

			Long endHour =
				parseIntegerRequired (
					requestContext.parameterRequired (
						"endHour"));

			Long dailyMax =
				parseIntegerRequired (
					requestContext.parameterRequired (
						"dailyMax"));

			if (radius < 1) {

				requestContext.addError (
					"Radius must be 1 or more");

				return null;

			}

			if (
				startHour < 0
				|| startHour > 23
				|| endHour < 0
				|| endHour > 23
			) {

				requestContext.addError (
					"Start and end hours must be between 0 and 23");

				return null;

			}

			if (dailyMax < 1) {

				requestContext.addError (
					"Daily max must be 1 or greater");

				return null;

			}

			ChatUserRec chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

			chatDateLogic.userDateStuff (
				transaction,
				chatUser,
				optionalOf (
					userConsoleLogic.userRequired (
						transaction)),
				optionalAbsent (),
				dateMode,
				radius,
				startHour,
				endHour,
				dailyMax,
				false);

			transaction.commit ();

			requestContext.addNotice (
				"Dating settings updated");

			return null;

		}

	}

}
