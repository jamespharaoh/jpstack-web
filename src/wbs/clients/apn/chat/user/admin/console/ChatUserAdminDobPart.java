package wbs.clients.apn.chat.user.admin.console;

import static wbs.framework.utils.etc.Misc.ifNull;

import javax.inject.Inject;

import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.cal.CalDate;

@PrototypeComponent ("chatUserAdminDobPart")
public
class ChatUserAdminDobPart
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
				CalDate.forLocalDate (chatUser.getDob ()),
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
