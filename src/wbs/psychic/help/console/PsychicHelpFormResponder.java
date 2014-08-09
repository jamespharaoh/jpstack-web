package wbs.psychic.help.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Set;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.context.ConsoleContext;
import wbs.platform.console.context.ConsoleContextScriptRef;
import wbs.platform.console.html.ScriptRef;
import wbs.platform.console.module.ConsoleManager;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.console.responder.HtmlResponder;
import wbs.psychic.help.model.PsychicHelpRequestRec;
import wbs.psychic.user.core.console.PsychicUserConsoleHelper;
import wbs.psychic.user.core.model.PsychicUserRec;

import com.google.common.collect.ImmutableSet;

@PrototypeComponent ("psychicHelpFormResponder")
public
class PsychicHelpFormResponder
	extends HtmlResponder {

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	ConsoleManager consoleManager;

	@Inject
	PsychicUserConsoleHelper psychicUserHelper;

	PsychicUserRec psychicUser;
	PsychicHelpRequestRec psychicHelpRequest;

	@Override
	protected
	void prepare () {

		psychicUser =
			psychicUserHelper.find (
				requestContext.stuffInt ("psychicUserId"));

		psychicHelpRequest =
			psychicUser.getHelpRequestsByIndex ().get (
				psychicUser.getNumHelpResponses ());

	}

	@Override
	protected
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				ConsoleContextScriptRef.javascript (
					"/js/gsm.js"))

			.add (
				ConsoleContextScriptRef.javascript (
					"/js/DOM.js"))

			.build ();

	}

	@Override
	public
	void goHeadStuff () {

		super.goHeadStuff ();

		ConsoleContext psychicUserContext =
			consoleManager.context (
				"psychicUser",
				true);

		printFormat (
			"<script language=\"javascript\">\n",

			"top.show_inbox (true);\n",

			"top.frames['main'].location = '%j';\n",
			requestContext.resolveApplicationUrl (
				stringFormat (
					"%s",
					psychicUserContext.pathPrefix (),
					"/%s",
					psychicUser.getId (),
					"/psychicUser.help")),

			"</script>\n");

	}

	@Override
	protected
	void goBodyStuff () {

		requestContext.flushNotices (out);

		printFormat (
			"<p",
			" class=\"links\"",
			"><a",
			" href=\"%h\"",
			requestContext.resolveApplicationUrl (
				"/queues/queue.home"),
			">Queues</a>\n");

		printFormat (
			"<a href=\"%h\">Close</a></p>\n",
			"javascript:top.show_inbox (false)");

		printFormat (
			"<h2>Respond to psychic help request</h2>\n");

		ConsoleContext psychicUserContext =
			consoleManager.context (
				"psychicUser",
				true);

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveContextUrl (
				stringFormat (
					"%s",
					psychicUserContext.pathPrefix (),
					"/%s",
					psychicUser.getId (),
					"/psychicUser.helpForm")),
			">\n");

		printFormat (
			"<input",
			" type=\"hidden\"",
			" name=\"psychicHelpRequestId\"",
			" value=\"%h\">\n",
			psychicHelpRequest.getId ());

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",

			"<th>User</th>\n",

			"<td>%h</td>\n",
			psychicUser.getCode (),

			"</tr>\n");

		printFormat (
			"<tr>\n",

			"<th>Request</th>\n",

			"<td>%h</td>\n",
			psychicHelpRequest.getRequestText (),

			"</tr>\n");

		printFormat (
			"<tr>\n",

			"<th>Message</th>\n",

			"<td colspan=\"2\">%s</td>\n",
			stringFormat (
				"<textarea",
				" name=\"text\"",
				" cols=\"64\"",
				" rows=\"4\"",

				" onkeyup=\"%h\"",
				stringFormat (
					"gsmCharCountMultiple2 (this, %s, %s)",
					"document.getElementById ('chars')",
					160 - ("From help: ").length ()),

				" onfocus=\"%h\"",
				stringFormat (
					"gsmCharCountMultiple2 (this, %s, %s)",
					"document.getElementById ('chars')",
					160 - ("From help: ").length ()),

				"></textarea>\n"),

			"</tr>\n");

		printFormat (
			"<tr>\n",

			"<th>Chars</th>\n",

			"<td>%s %s</td>\n",
			"<span id=\"chars\">&nbsp;</span>",
			"<span id=\"messageCount\">&nbsp;</span>",

			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"<p>",

			"<input" +
			" type=\"submit\"" +
			" name=\"send\"" +
			" value=\"send message\">\n",

			/*
			"<input" +
			" type=\"submit\"" +
			" name=\"sendAndNote\"" +
			" value=\"send and make note\">\n",
			*/

			/*
			"<input" +
			" type=\"submit\"" +
			" name=\"ignore\"" +
			" value=\"don't send anything\">",
			*/

			"</p>\n");

		printFormat (
			"</form>\n");

	}

}
