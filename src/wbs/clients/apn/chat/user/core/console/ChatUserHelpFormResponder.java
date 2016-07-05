package wbs.clients.apn.chat.user.core.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.HtmlResponder;
import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("chatUserHelpFormResponder")
public
class ChatUserHelpFormResponder
	extends HtmlResponder {

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	@Inject
	ConsoleRequestContext requestContext;

	ChatUserRec chatUser;

	@Override
	public
	void prepare () {

		chatUser =
			chatUserHelper.findOrNull (
				requestContext.stuffInt ("chatUserId"));
	}

	@Override
	public
	void renderHtmlBodyContents () {

		printFormat (
			"<h2>Send help message</h2>\n");

		requestContext.flushNotices (
			requestContext.writer ());

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/chatUser.helpForm"),
			">\n");

		printFormat (
			"<table class=\"details\">\n");

		String userInfo =
			chatUser.getName () == null
				? chatUser.getCode ()
				: chatUser.getCode () + " " + chatUser.getName ();

		printFormat (
			"<tr>\n",
			"<th>User</th>\n",

			"<td>%h</td>\n",
			userInfo,

			"</tr>\n");

		String charCountScript =
			stringFormat (
				"gsmCharCount (%s, %s, 149)",
				"document.getElementById ('text')",
				"document.getElementById ('chars')");

		printFormat (
			"<tr>\n",
			"<th>Message</th>\n",

			"<td><textarea",
			" id=\"text\"",
			" cols=\"64\"",
			" rows=\"4\"",
			" name=\"text\"",
			" onkeyup=\"%h\"",
			charCountScript,
			" onfocus=\"%h\"",
			charCountScript,
			"></textarea></td>\n",

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Chars</th>\n",

			"<td><span id=\"chars\">&nbsp;</span></td>\n",

			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"send message\"",
			"></p>\n");

		printFormat (
			"</form>\n");

		printFormat (
			"<script type=\"text/javascript\">\n",
			"%s;\n",
			charCountScript,
			"</script>\n");

	}

}
