package wbs.apn.chat.user.admin.console;

import static wbs.utils.etc.EnumUtils.enumEqualSafe;

import java.util.Set;

import javax.inject.Provider;

import com.google.common.collect.ImmutableSet;

import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.utils.web.HtmlTableCheckWriter;

@PrototypeComponent ("chatUserAdminBarringPart")
public
class ChatUserAdminBarringPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <HtmlTableCheckWriter> htmlTableCheckWriterProvider;

	// state

	ChatUserRec chatUser;

	// details

	@Override
	public
	Set <ScriptRef> scriptRefs () {

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
				"<th>Action</th>");

			if (chatUser.getBarred ()) {

				htmlTableCheckWriterProvider.get ()

					.name (
						"bar_off")

					.label (
						"remove bar")

					.value (
						false)

					.write (
						formatWriter);

			} else {

				htmlTableCheckWriterProvider.get ()

					.name (
						"bar_off")

					.label (
						"bar user")

					.value (
						false)

					.write (
						formatWriter);

			}

			printFormat (
				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Reason</th>\n");

			printFormat (
				"<td><textarea",
				" rows=\"4\"",
				" cols=\"48\"",
				" name=\"reason\"></textarea></td>\n");

			printFormat (
				"</tr>\n");

			printFormat (
				"<tr>\n",
				"<th>Action</th>\n");

			printFormat (
				"<td><input",
				" type=\"submit\"",
				" value=\"save changes\"",
				"></td>\n");

			printFormat (
				"</tr>\n");

		}

		printFormat (
			"</table>\n");

		printFormat (
			"</form>\n");

		requestContext.flushScripts ();

	}

}
