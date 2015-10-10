package wbs.clients.apn.chat.user.admin.console;

import javax.inject.Inject;

import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.Gender;
import wbs.clients.apn.chat.user.core.model.Orient;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("chatUserAdminPrefsPart")
public
class ChatUserAdminPrefsPart
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

		if (chatUser == null
				|| chatUser.getGender () == null
				|| chatUser.getOrient () == null) {

			requestContext.addError (
				"Cannot change prefs for this user");

			requestContext.flushNotices ();

			return;

		}

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/chatUser.admin.prefs"),
			">\n");

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",
			"<th>Code</th>\n",
			"<td>%h</td>\n",
			chatUser.getCode (),
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Gender</th>\n",
			"<td><select name=\"gender\">\n");

		if (chatUser.getGender () == Gender.male) {

			printFormat (
				"<option selected>male</option>\n",
				"<option>female</option>\n");

		} else if (chatUser.getGender () == Gender.female) {

			printFormat (
				"<option>male</option>\n",
				"<option selected>female</option>\n");

		} else if (chatUser.getGender () == null) {

			printFormat (
				"<option>\n",
				"<option>male</option>\n",
				"<option>female</option>\n");

		} else {

			throw new RuntimeException ();

		}

		printFormat (
			"</select></td>\n",

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Orient</th>\n",

			"<td><select name=\"orient\">\n");

		if (chatUser.getOrient () == Orient.gay) {

			printFormat (
				"<option selected>gay</option>\n",
				"<option>bi</option>\n",
				"<option>straight</option>\n");

		} else if (chatUser.getOrient () == Orient.bi) {

			printFormat (
				"<option>gay</option>\n",
				"<option selected>bi</option>\n",
				"<option>straight</option>\n");

		} else if (chatUser.getOrient () == Orient.straight) {

			printFormat (
				"<option>gay</option>\n",
				"<option>bi</option>\n",
				"<option selected>straight</option>\n");

		} else {

			printFormat (
				"<option>\n",
				"<option>gay</option>\n",
				"<option>bi</option>\n",
				"<option>straight</option>\n");

		}

		printFormat (
			"</select></td>\n",

			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"update prefs\"",
			"></p>\n");
	}

}
