package wbs.clients.apn.chat.help.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.google.common.collect.ImmutableSet;

import wbs.clients.apn.chat.help.model.ChatHelpLogRec;
import wbs.clients.apn.chat.help.model.ChatHelpTemplateRec;
import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.HtmlResponder;
import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("chatHelpLogPendingFormResponder")
public
class ChatHelpLogPendingFormResponder
	extends HtmlResponder {

	// dependencies

	@Inject
	ChatHelpLogConsoleHelper chatHelpLogHelper;

	@Inject
	ChatHelpTemplateConsoleHelper chatHelpTemplateHelper;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	UserPrivChecker privChecker;

	// state

	ChatHelpLogRec chatHelpLog;
	List<ChatHelpTemplateRec> chatHelpTemplates;

	// details

	@Override
	protected
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/gsm.js"))

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/DOM.js"))

			.build ();

	}

	// implementation

	@Override
	public
	void prepare () {

		chatHelpLog =
			chatHelpLogHelper.find (
				requestContext.stuffInt ("chatHelpLogId"));

		chatHelpTemplates =
			chatHelpTemplateHelper.findByParentAndType (
				chatHelpLog.getChatUser ().getChat (),
				"help");

		Collections.sort (
			chatHelpTemplates);

	}

	@Override
	public
	void renderHtmlHeadContents () {

		super.renderHtmlHeadContents ();

		printFormat (
			"<script language=\"javascript\">\n");

		printFormat (
			"top.show_inbox (true);\n",
			"top.frames ['main'].location = '%j';\n",
			requestContext.resolveApplicationUrl (
				stringFormat (
					"/chatHelpLog.pending",
					"/%u",
					chatHelpLog.getId (),
					"/chatHelpLog.pending.summary")),

			"var helpTemplates = new Array ();\n");

		for (
			ChatHelpTemplateRec chatHelpTemplate
				: chatHelpTemplates
		) {

			printFormat (
				"helpTemplates [%s] = '%j';\n",
				chatHelpTemplate.getId (),
				chatHelpTemplate.getText ());

		}

		printFormat (
			"function useTemplate () {\n",
			"  var templateId = document.getElementById ('template_id');\n",
			"  var text = document.getElementById ('text');\n",
			"  if (templateId.value == '') return;\n",
			"  var template = helpTemplates[templateId.value];\n",
			"  if (template) text.value = template;\n",
			"}");

		printFormat (
			"function showReply () {\n",
			"}\n");

		printFormat (
			"function showInfo () {\n",
			"}\n");

		printFormat (
			"</script>\n");

	}

	@Override
	public
	void renderHtmlBodyContents () {

		requestContext.flushNotices ();

		printFormat (
			"<p class=\"links\"><a href=\"%h\">Queues</a>\n",
			requestContext.resolveApplicationUrl (
				"/queues/queue.home"),

			"<a href=\"%h\">Close</a></p>\n",
			"javascript:top.show_inbox (false);");

		printFormat (
			"<h2>Respond to chat help request</h2>\n");

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/chatHelpLog.pending.form"),
			">\n");

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",
			"<th>Request</th>\n",

			"<td>%h</td>\n",
			chatHelpLog.getText (),

			"</tr>");

		printFormat (
			"<tr>\n",
			"<th>Options</th>\n",

			"<td><input",
			" type=\"button\"",
			" onclick=\"showReply ()\"",
			" value=\"reply\"",
			">",

			"<input",
			" type=\"button\"",
			" onclick=\"showInfo ()\"",
			" value=\"change info\"",
			"></td>\n",

			"</tr>\n");

		printFormat (
			"<tr id=\"replyTemplateRow\">\n",
			"<th>Template</th>\n",

			"<td><select id=\"template_id\">\n",
			"<option>\n");

		for (ChatHelpTemplateRec chatHelpTemplate
				: chatHelpTemplates) {

			printFormat (
				"<option value=\"%h\">%h</option>\n",
				chatHelpTemplate.getId (),
				chatHelpTemplate.getCode ());

		}

		printFormat (
			"</select>\n",

			"<input",
			" type=\"button\"",
			" onclick=\"useTemplate ()\"",
			" value=\"ok\"></td>\n",

			"</tr>");

		printFormat (
			"<tr id=\"replyTextRow\">\n",

			"<th>Reply</th>\n",

			"<td><textarea",
			" id=\"text\"",
			" name=\"text\"",
			" cols=\"64\"",
			" rows=\"3\"",
			" onkeyup=\"%h\"",
			stringFormat (
				"gsmCharCount (%s, %s, %s)",
				"this",
				"document.getElementById ('chars')",
				"149"),
			" onfocus=\"%h\"",
			stringFormat (
				"gsmCharCount (%s, %s, %s)",
				"this",
				"document.getElementById ('chars')",
				"149"),
			"></textarea></td>\n",

			"</tr>\n");

		printFormat (
			"<tr id=\"replyCharsRow\">\n",
			"<th>Chars</th>\n",
			"<td><span id=\"chars\">&nbsp;</span></td>\n",
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Actions</th>\n",

			"<td><input",
			" id=\"replyButton\"",
			" type=\"submit\"",
			" name=\"reply\"",
			" value=\"send reply\"",
			">",

			"<input",
			" style=\"display: none\"",
			" id=\"infoButton\"",
			" type=\"submit\"",
			" name=\"info\"",
			" value=\"change info\"",
			">");

		if (requestContext.canContext ("chat.supervisor")) {

			printFormat (
				"<input",
				" id=\"ignoreButton\"",
				" type=\"submit\"",
				" name=\"ignore\"",
				" value=\"ignore request\">");

		}

		printFormat (
			"</td>\n",
			"</tr>");

		printFormat (
			"</table>\n");

		printFormat (
			"</form>\n");

	}

}
