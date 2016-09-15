package wbs.apn.chat.user.admin.console;

import static wbs.utils.etc.LogicUtils.anyOf;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalValueNotEqualSafe;
import static wbs.utils.string.StringUtils.nullIfEmptyString;

import com.google.common.base.Optional;

import lombok.Cleanup;

import wbs.apn.chat.user.core.model.Gender;
import wbs.apn.chat.user.core.model.Orient;
import wbs.apn.chat.user.core.model.ChatUserObjectHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleLogic;

@PrototypeComponent ("chatUserAdminPrefsAction")
public
class ChatUserAdminPrefsAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ChatUserObjectHelper chatUserHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// details

	@Override
	public
	Responder backupResponder () {

		return responder (
			"chatUserAdminPrefsResponder");

	}

	// implementation

	@Override
	public
	Responder goReal () {

		// get stuff

		Long chatUserId =
			requestContext.stuffInteger (
				"chatUserId");

		// get params

		String genderParam =
			nullIfEmptyString (
				requestContext.parameterRequired (
					"gender"));

		String orientParam =
			nullIfEmptyString (
				requestContext.parameterRequired (
					"orient"));

		// check params

		if (genderParam == null || orientParam == null) {

			requestContext.addError (
				"Please select a gender and an orient");

			return null;

		}

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatUserAdminPrefsAction.goReal ()",
				this);

		// lookup database stuff

		ChatUserRec chatUser =
			chatUserHelper.findRequired (
				chatUserId);

		// check changes

		Optional <Gender> oldGenderOptional =
			optionalFromNullable (
				chatUser.getGender ());

		Gender newGender =
			Gender.valueOf (
				genderParam);

		Optional <Orient> oldOrientOptional =
			optionalFromNullable (
				chatUser.getOrient ());

		Orient newOrient =
			Orient.valueOf (
				orientParam);

		if (anyOf (

			() -> optionalValueNotEqualSafe (
				oldGenderOptional,
				newGender),

			() -> optionalValueNotEqualSafe (
				oldOrientOptional,
				newOrient)

		)) {

			chatUser

				.setGender (
					newGender)

				.setOrient (
					newOrient);

			eventLogic.createEvent (
				"chat_user_prefs",
				userConsoleLogic.userRequired (),
				chatUser,
				chatUser.getGender ().toString (),
				chatUser.getOrient ().toString ());

		}

		transaction.commit ();

		requestContext.addNotice (
			"Chat user prefs updated");

		return null;

	}

}
