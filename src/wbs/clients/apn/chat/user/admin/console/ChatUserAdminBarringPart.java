package wbs.clients.apn.chat.user.admin.console;

import static wbs.framework.utils.etc.Misc.equal;

import java.util.Set;

import javax.inject.Inject;

import wbs.clients.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.clients.apn.chat.user.core.model.ChatUserType;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.context.ConsoleApplicationScriptRef;
import wbs.platform.console.html.ScriptRef;
import wbs.platform.console.part.AbstractPagePart;

import com.google.common.collect.ImmutableSet;

@PrototypeComponent ("chatUserAdminBarringPart")
public
class ChatUserAdminBarringPart
	extends AbstractPagePart {

	@Inject
	ChatUserConsoleHelper chatUserHelper;

	ChatUserRec chatUser;

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/wbs.js"))

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/DOM.js"))

			.build ();

	}

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

		if (
			equal (
				chatUser.getType (),
				ChatUserType.monitor)
		) {

			printFormat (
				"<p>This is a monitor and cannot be barred.</p>\n");

			return;

		}

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/chatUser.admin.barring"),
			">\n");

		printFormat (
			"<table",
			" class=\"details\"",
			">\n");

		printFormat (
			"<tr>\n",
			"<th>Status</th>\n",
			"<td>%h</td>\n",
			chatUser.getBarred ()
				? "barred"
				: "not barred",
			"</tr>\n");

		if (requestContext.canContext ("chat.userAdmin")) {

			printFormat (
				"<tr>\n",
				"<th>Action</th>",

				"%s\n",
				chatUser.getBarred ()
					? requestContext.magicTdCheck (
						"bar_off",
						"remove bar",
						false)
					: requestContext.magicTdCheck (
						"bar_on",
						"bar user",
						false),

				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Reason</th>\n",

				"<td><textarea",
				" rows=\"4\"",
				" cols=\"48\"",
				" name=\"reason\"></textarea></td>\n",

				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Action</th>\n",

				"<td><input",
				" type=\"submit\"",
				" value=\"save changes\"",
				"></td>\n",

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

		printFormat (
			"</form>\n");

		requestContext.flushScripts ();

	}

}
