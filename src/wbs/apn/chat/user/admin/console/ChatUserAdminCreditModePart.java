package wbs.apn.chat.user.admin.console;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.ifNull;

import javax.inject.Inject;

import wbs.apn.chat.core.console.ChatUserCreditModeConsoleHelper;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.part.AbstractPagePart;

@PrototypeComponent ("chatUserAdminCreditModePart")
public
class ChatUserAdminCreditModePart
	extends AbstractPagePart {

	@Inject
	ChatUserCreditModeConsoleHelper chatUserCreditModeConsoleHelper;

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
	void goBodyStuff () {

		if (
			equal (
				chatUser.getType (),
				ChatUserType.monitor)
		) {

			printFormat (
				"<p>This is a monitor and has no credit mode.</p>\n");

			return;

		}

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/chatUser.admin.creditMode"),
			">\n");

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",
			"<th>Credit mode</th>\n",

			"<td>%s</td>\n",
			chatUserCreditModeConsoleHelper.select (
				"creditMode",
				ifNull (
					requestContext.getForm ("creditMode"),
					chatUser.getCreditMode ().toString ())),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Actions</th>\n",

			"<td><input",
			" type=\"submit\"",
			" value=\"change mode\"",
			"></td>\n",

			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"</form>\n");

	}

}
