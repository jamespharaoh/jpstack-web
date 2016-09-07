package wbs.clients.apn.chat.user.admin.console;

import static wbs.framework.utils.etc.NullUtils.ifNull;

import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.utils.TimeFormatter;

@PrototypeComponent ("chatUserAdminDobPart")
public
class ChatUserAdminDobPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	TimeFormatter timeFormatter;

	// state

	ChatUserRec chatUser;

	// implementation

	@Override
	public
	void prepare () {

		chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInteger (
					"chatUserId"));

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\">\n",
			requestContext.resolveLocalUrl (
				"/chatUser.admin.dob"));

		printFormat (
			"<p>Date of birth (yyyy-mm-dd)<br>\n",

			"<input",
			" type=\"text\"",
			" name=\"dob\"",
			" value=\"%h\"></p>\n",
			ifNull (
				requestContext.getForm ("dob"),
				timeFormatter.dateString (
					chatUser.getDob ()),
				""));

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"update date of birth\"",
			"><p>\n");

		printFormat (
			"</form>\n");

	}

}
