package wbs.clients.apn.chat.user.admin.console;

import static wbs.framework.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.framework.utils.etc.NullUtils.ifNull;

import javax.inject.Inject;
import javax.inject.Named;

import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.ChatUserType;
import wbs.console.helper.EnumConsoleHelper;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.SingletonDependency;

@PrototypeComponent ("chatUserAdminCreditModePart")
public
class ChatUserAdminCreditModePart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	@Named
	EnumConsoleHelper <?> chatUserCreditModeConsoleHelper;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	// state

	ChatUserRec chatUser;

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

		if (
			enumEqualSafe (
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
