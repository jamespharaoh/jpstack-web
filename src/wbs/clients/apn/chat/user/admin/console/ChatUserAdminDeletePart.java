package wbs.clients.apn.chat.user.admin.console;

import static wbs.framework.utils.etc.Misc.allOf;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNull;

import javax.inject.Inject;

import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.ChatUserType;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("chatUserAdminDeletePart")
public
class ChatUserAdminDeletePart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ConsoleObjectManager consoleObjectManager;

	// state

	ChatUserRec chatUser;
	ChatUserRec newChatUser;

	// implementation

	@Override
	public
	void prepare () {

		chatUser =
			chatUserHelper.find (
				requestContext.stuffInt ("chatUserId"));

		if (chatUser.getOldNumber () != null) {

			newChatUser =
				chatUserHelper.find (
					chatUser.getChat (),
					chatUser.getOldNumber ());

		}

	}

	@Override
	public
	void renderHtmlBodyContent () {

		// can't delete monitors

		if (chatUser.getType () != ChatUserType.user) {

			printFormat (
				"<p>Monitors can not be deleted.</p>");

			return;

		}

		// information about this user

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",
			"<th>User ID</th>\n",
			"<td>%h</td>\n",
			chatUser.getId (),
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Code</th>\n",
			"<td>%h</td>\n",
			chatUser.getCode (),
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Number</th>\n",
			"%s\n",
			consoleObjectManager.tdForObjectMiniLink (
				chatUser.getOldNumber ()),
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Deleted</th>\n",
			"<td>%h</td>\n",
			chatUser.getNumber () == null
				? "yes"
				: "no",
			"</tr>\n");

		printFormat (
			"</table>\n");

		// general information

		printFormat (
			"<p>The delete function simply removes the connection between the ",
			"user and their phone number. This appears to the customer as if ",
			"their profile has gone, while still allowing us to view ",
			"historical information in the console.</p>\n");

		// action button, or excuse for having none

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/chatUser.admin.delete"),
			">\n");

		if (chatUser.getNumber () != null) {

			printFormat (
				"<p><input",
				" type=\"submit\"",
				" name=\"deleteUser\"",
				" value=\"delete user\"",
				"></p>\n");

		} else if (
			allOf (
				isNotNull (chatUser.getOldNumber ()),
				isNull (newChatUser))
		) {

			printFormat (
				"<p><input",
				" type=\"submit\"",
				" name=\"undeleteUser\"",
				" value=\"undelete user\"",
				"></p>\n");

		} else if (
			allOf (
				isNull (chatUser.getOldNumber ()),
				isNotNull (newChatUser))
		) {

			printFormat (
				"<p>This user cannot be undeleted, because there is a new ",
				"chat user profile with the same number.</p>\n");

		} else if (
			isNull (chatUser.getOldNumber ())
		) {

			printFormat (
				"<p>This user cannot be undeleted because the phone number ",
				"it was associated with has been deleted from the ",
				"system.</p>\n");

		}

		printFormat (
			"</form>\n");

	}

}
