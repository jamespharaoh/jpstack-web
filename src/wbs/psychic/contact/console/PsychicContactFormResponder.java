package wbs.psychic.contact.console;

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
import wbs.psychic.contact.model.PsychicContactRec;
import wbs.psychic.profile.model.PsychicProfileRec;
import wbs.psychic.request.model.PsychicRequestRec;
import wbs.psychic.user.core.model.PsychicUserRec;

import com.google.common.collect.ImmutableSet;

@PrototypeComponent ("psychicContactFormResponder")
public
class PsychicContactFormResponder
	extends HtmlResponder {

	@Inject
	PsychicContactConsoleHelper psychicContactHelper;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	ConsoleManager consoleManager;

	PsychicContactRec contact;
	PsychicRequestRec request;
	PsychicProfileRec profile;
	PsychicUserRec user;

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
	protected
	void prepare () {

		contact =
			psychicContactHelper.find (
				requestContext.stuffInt ("psychicContactId"));

		request =
			contact.getRequestsByIndex ().get (
				contact.getNumResponses ());

		profile =
			contact.getPsychicProfile ();

		user =
			contact.getPsychicUser ();

	}

	@Override
	public
	void goHeadStuff () {

		super.goHeadStuff ();

		ConsoleContext psychicContactContext =
			consoleManager.context (
				"psychicContact",
				true);

		printFormat (
			"<script language=\"javascript\">\n",

			"top.show_inbox (true);\n",

			"top.frames['main'].location = '%j';\n",
			requestContext.resolveApplicationUrl (
				stringFormat (
					"%s/%s/psychicContact_summary",
					psychicContactContext.pathPrefix (),
					contact.getId ())),

			"</script>\n");

	}

	@Override
	public
	void goBodyStuff () {

		requestContext.flushNotices (out);

		printFormat (
			"<p class=\"links\"><a href=\"%h\">Queues</a>\n",
			requestContext.resolveApplicationUrl (
				"/queues/queue.home"));

		printFormat (
			"<a href=\"%h\">Close</a></p>\n",
			"javascript:top.show_inbox (false)");

		printFormat (
			"<h2>Send message as psychic</h2>\n");

		ConsoleContext psychicContactContext =
			consoleManager.context (
				"psychicContact",
				true);

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveApplicationUrl (
				stringFormat (
					"%s",
					psychicContactContext.pathPrefix (),
					"/%u",
					contact.getId (),
					"/psychicContact.form")),
			">\n");

		printFormat (
			"<input",
			" type=\"hidden\"",
			" name=\"psychicRequestId\"",
			" value=\"%h\">\n",
			request.getId ());

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",

			"<th>Profile</th>\n",

			"<td>%h</td>\n",
			profile.getName (),

			"</tr>\n");

		printFormat (
			"<tr>\n",

			"<th>User</th>\n",

			"<td>%h</td>\n",
			user.getCode (),

			"</tr>\n");

		printFormat (
			"<tr>\n",

			"<th>Request</th>\n",

			"<td>%h</td>\n",
			request.getRequestText (),

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
					160 - ("From " + profile.getName () + ": ").length ()),

				" onfocus=\"%h\"",
				stringFormat (
					"gsmCharCountMultiple2 (this, %s, %s)",
					"document.getElementById ('chars')",
					160 - ("From " + profile.getName () + ": ").length ()),

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
