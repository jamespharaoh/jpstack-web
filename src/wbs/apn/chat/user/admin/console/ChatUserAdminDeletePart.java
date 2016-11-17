package wbs.apn.chat.user.admin.console;

import static wbs.utils.etc.LogicUtils.allOf;
import static wbs.utils.etc.LogicUtils.booleanToYesNo;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteRaw;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import lombok.NonNull;

import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("chatUserAdminDeletePart")
public
class ChatUserAdminDeletePart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ConsoleObjectManager consoleObjectManager;

	// state

	ChatUserRec chatUser;
	ChatUserRec newChatUser;

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInteger (
					"chatUserId"));

		if (
			isNotNull (
				chatUser.getOldNumber ())
		) {

			newChatUser =
				chatUserHelper.find (
					chatUser.getChat (),
					chatUser.getOldNumber ());

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		// can't delete monitors

		if (chatUser.getType () != ChatUserType.user) {

			formatWriter.writeLineFormat (
				"<p>Monitors can not be deleted.</p>");

			return;

		}

		// information about this user

		htmlTableOpenDetails ();

		htmlTableDetailsRowWrite (
			"User ID",
			integerToDecimalString (
				chatUser.getId ()));

		htmlTableDetailsRowWrite (
			"Code",
			chatUser.getCode ());

		htmlTableDetailsRowWriteRaw (
			"Number",
			() -> consoleObjectManager.writeTdForObjectMiniLink (
				chatUser.getOldNumber ()));

		htmlTableDetailsRowWrite (
			"Deleted",
			booleanToYesNo (
				isNotNull (
					chatUser.getNumber ())));

		htmlTableClose ();

		// general information

		formatWriter.writeFormat (
			"<p>The delete function simply removes the connection between the ",
			"user and their phone number. This appears to the customer as if ",
			"their profile has gone, while still allowing us to view ",
			"historical information in the console.</p>");

		// action button, or excuse for having none

		htmlFormOpenPostAction (
			requestContext.resolveLocalUrl (
				"/chatUser.admin.delete"));

		if (
			isNotNull (
				chatUser.getNumber ())
		) {

			formatWriter.writeLineFormat (
				"<p><input",
				" type=\"submit\"",
				" name=\"deleteUser\"",
				" value=\"delete user\"",
				"></p>");

		} else if (allOf (

			() -> isNotNull (
				chatUser.getOldNumber ()),

			() -> isNull (
				newChatUser)

		)) {

			formatWriter.writeLineFormat (
				"<p><input",
				" type=\"submit\"",
				" name=\"undeleteUser\"",
				" value=\"undelete user\"",
				"></p>");

		} else if (allOf (

			() -> isNull (
				chatUser.getOldNumber ()),

			() -> isNotNull (
				newChatUser)

		)) {

			formatWriter.writeLineFormat (
				"<p>This user cannot be undeleted, because there is a new ",
				"chat user profile with the same number.</p>");

		} else if (
			isNull (chatUser.getOldNumber ())
		) {

			formatWriter.writeLineFormat (
				"<p>This user cannot be undeleted because the phone number ",
				"it was associated with has been deleted from the ",
				"system.</p>");

		}

		htmlFormClose ();

	}

}
