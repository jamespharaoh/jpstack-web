package wbs.apn.chat.settings.console;

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

import wbs.apn.chat.core.console.ChatConsoleHelper;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.Gender;
import wbs.apn.chat.user.core.model.Orient;
import wbs.web.responder.Responder;

@PrototypeComponent ("chatSettingsMonitorsAction")
public
class ChatSettingsMonitorsAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ChatMiscLogic chatMiscLogic;

	@SingletonDependency
	ChatConsoleHelper chatHelper;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// details

	@Override
	public
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return responder (
			"chatSettingsMonitorsResponder");

	}

	@Override
	public
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWriteWithoutParameters (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

			if (! requestContext.canContext ("chat.manage")) {

				requestContext.addError (
					"Access denied");

				return null;

			}

			long gayMale;
			long gayFemale;
			long biMale;
			long biFemale;
			long straightMale;
			long straightFemale;

			try {

				gayMale =
					requestContext.parameterIntegerRequired (
						"gayMale");

				gayFemale =
					requestContext.parameterIntegerRequired (
						"gayFemale");

				biMale =
					requestContext.parameterIntegerRequired (
						"biMale");

				biFemale =
					requestContext.parameterIntegerRequired (
						"biFemale");

				straightMale =
					requestContext.parameterIntegerRequired (
						"straightMale");

				straightFemale =
					requestContext.parameterIntegerRequired (
						"straightFemale");

			} catch (NumberFormatException exception) {

				requestContext.addError (
					"Please enter a real number in each box.");

				return null;

			}

			// perform action

			ChatRec chat =
				chatHelper.findFromContextRequired (
					transaction);

			chatMiscLogic.monitorsToTarget (
				transaction,
				chat,
				Gender.male,
				Orient.gay,
				gayMale);

			chatMiscLogic.monitorsToTarget (
				transaction,
				chat,
				Gender.female,
				Orient.gay,
				gayFemale);

			chatMiscLogic.monitorsToTarget (
				transaction,
				chat,
				Gender.male,
				Orient.bi,
				biMale);

			chatMiscLogic.monitorsToTarget (
				transaction,
				chat,
				Gender.female,
				Orient.bi,
				biFemale);

			chatMiscLogic.monitorsToTarget (
				transaction,
				chat,
				Gender.male,
				Orient.straight,
				straightMale);

			chatMiscLogic.monitorsToTarget (
				transaction,
				chat,
				Gender.female,
				Orient.straight,
				straightFemale);

			transaction.commit ();

			requestContext.addNotice (
				"Chat monitors updated");

			requestContext.setEmptyFormData ();

			return null;

		}

	}

}
