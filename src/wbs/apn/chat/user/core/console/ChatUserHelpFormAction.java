package wbs.apn.chat.user.core.console;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;

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

import wbs.sms.gsm.GsmUtils;

import wbs.apn.chat.help.logic.ChatHelpLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.web.responder.Responder;

@PrototypeComponent ("chatUserHelpFormAction")
public
class ChatUserHelpFormAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ChatHelpLogic chatHelpLogic;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// details

	@Override
	public
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return responder (
			"chatUserHelpFormResponder");

	}

	// implementation

	@Override
	protected
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWriteWithoutParameters (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

			// get parameters

			String text =
				requestContext.parameterRequired (
					"text");

			// check parameters

			if (text.length() == 0) {
				requestContext.addError("Please type a message");
				return null;
			}

			if (!GsmUtils.gsmStringIsValid(text)) {
				requestContext.addError("Reply contains invalid characters");
				return null;
			}

			if (GsmUtils.gsmStringLength(text) > 149) {

				requestContext.addError (
					"Text is too long!");

				return null;

			}

			// get objects

			ChatUserRec chatUser =
				chatUserHelper.findFromContextRequired (
					transaction);

			// send message

			chatHelpLogic.sendHelpMessage (
				transaction,
				userConsoleLogic.userRequired (
					transaction),
				chatUser,
				text,
				optionalAbsent (),
				optionalAbsent ());

			transaction.commit ();

			// return

			requestContext.addNotice (
				"Message sent");

			return null;

		}

	}

}
