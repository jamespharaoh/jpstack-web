package wbs.clients.apn.chat.user.admin.console;

import javax.inject.Inject;

import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.ChatUserType;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.part.AbstractPagePart;

@PrototypeComponent ("chatUserAdminOnlinePart")
public
class ChatUserAdminOnlinePart
	extends AbstractPagePart {

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	ChatUserRec chatUser;

	@Override
	public
	void prepare () {

		chatUser =
			chatUserHelper.find (
				requestContext.stuffInt ("chatUserId"));
	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/chatUser.admin.online"),
			">\n");

		if (chatUser.getOnline ()) {

			printFormat (
				"<p>This user is online</p>\n");

			printFormat (
				"<p><input",
				" type=\"submit\"",
				" name=\"offline\"",
				" value=\"take offline\"",
				"></p>\n");

		} else {

			printFormat (
				"<p>This user is offline</p>\n");

			if (
				chatUser.getType () == ChatUserType.user
				&& chatUser.getFirstJoin () == null
			) {

				printFormat (
					"<p>This user has never been online before, please don't ",
					"bring them online unless you are sure!</p>\n");

			}

			printFormat (
				"<p><input",
				" type=\"submit\"",
				" name=\"online\"",
				" value=\"bring online\"",
				"></p>\n");

		}

		printFormat (
			"</table>\n");

	}

}
