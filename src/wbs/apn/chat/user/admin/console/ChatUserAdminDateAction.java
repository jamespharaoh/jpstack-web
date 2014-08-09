package wbs.apn.chat.user.admin.console;

import static wbs.framework.utils.etc.Misc.toEnum;
import static wbs.framework.utils.etc.Misc.toInteger;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.apn.chat.date.logic.ChatDateLogic;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserDateMode;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("chatUserAdminDateAction")
public
class ChatUserAdminDateAction
	extends ConsoleAction {

	@Inject
	ChatDateLogic chatDateLogic;

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	UserObjectHelper userHelper;

	@Override
	public
	Responder backupResponder () {
		return responder ("chatUserAdminDateResponder");

	}

	@Override
	public
	Responder goReal () {

		ChatUserDateMode dateMode =
			toEnum (
				ChatUserDateMode.class,
				requestContext.parameter ("dateMode"));

		Integer radius =
			toInteger (requestContext.parameter ("radius"));

		Integer startHour =
			toInteger (requestContext.parameter ("startHour"));

		Integer endHour =
			toInteger (requestContext.parameter ("endHour"));

		Integer dailyMax =
			toInteger (requestContext.parameter ("dailyMax"));

		if (radius == null
				|| startHour == null
				|| endHour == null
				|| dailyMax == null) {

			requestContext.addError (
				"Please fill in the form properly.");

			return null;

		}

		if (radius < 1) {

			requestContext.addError (
				"Radius must be 1 or more");

			return null;

		}

		if (startHour < 0
				|| startHour > 23
				|| endHour < 0
				|| endHour > 23) {

			requestContext.addError (
				"Start and end hours must be between 0 and 23");

			return null;

		}

		if (dailyMax < 1) {

			requestContext.addError (
				"Daily max must be 1 or greater");

			return null;

		}

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		ChatUserRec chatUser =
			chatUserHelper.find (
				requestContext.stuffInt ("chatUserId"));

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		chatDateLogic.userDateStuff (
			chatUser,
			myUser,
			null,
			dateMode,
			radius,
			startHour,
			endHour,
			dailyMax,
			false);

		transaction.commit ();

		requestContext.addNotice ("Dating settings updated");

		return null;
	}

}
