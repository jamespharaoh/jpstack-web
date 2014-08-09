package wbs.apn.chat.settings.console;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.apn.chat.core.logic.ChatMiscLogic;
import wbs.apn.chat.core.model.ChatObjectHelper;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.model.Gender;
import wbs.apn.chat.user.core.model.Orient;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.request.ConsoleRequestContext;

@PrototypeComponent ("chatSettingsMonitorsAction")
public
class ChatSettingsMonitorsAction
	extends ConsoleAction {

	@Inject
	ChatMiscLogic chatMiscLogic;

	@Inject
	ChatObjectHelper chatHelper;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Override
	public
	Responder backupResponder () {
		return responder ("chatSettingsMonitorsResponder");
	}

	@Override
	public
	Responder goReal () {

		if (! requestContext.canContext ("chat.manage")) {

			requestContext.addError ("Access denied");

			return null;

		}

		int gayMale;
		int gayFemale;
		int biMale;
		int biFemale;
		int straightMale;
		int straightFemale;

		try {

			gayMale =
				Integer.parseInt (
					requestContext.parameter ("gayMale"));

			gayFemale =
				Integer.parseInt (
					requestContext.parameter ("gayFemale"));

			biMale =
				Integer.parseInt (
					requestContext.parameter ("biMale"));

			biFemale =
				Integer.parseInt (
					requestContext.parameter ("biFemale"));

			straightMale =
				Integer.parseInt (
					requestContext.parameter ("straightMale"));

			straightFemale =
				Integer.parseInt (
					requestContext.parameter ("straightFemale"));

		} catch (NumberFormatException exception) {

			requestContext.addError (
				"Please enter a real number in each box.");

			return null;

		}

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		ChatRec chat =
			chatHelper.find (
				requestContext.stuffInt ("chatId"));

		chatMiscLogic.monitorsToTarget (
			chat,
			Gender.male,
			Orient.gay,
			gayMale);

		chatMiscLogic.monitorsToTarget (
			chat,
			Gender.female,
			Orient.gay,
			gayFemale);

		chatMiscLogic.monitorsToTarget (
			chat,
			Gender.male,
			Orient.bi,
			biMale);

		chatMiscLogic.monitorsToTarget (
			chat,
			Gender.female,
			Orient.bi,
			biFemale);

		chatMiscLogic.monitorsToTarget (
			chat,
			Gender.male,
			Orient.straight,
			straightMale);

		chatMiscLogic.monitorsToTarget (
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
