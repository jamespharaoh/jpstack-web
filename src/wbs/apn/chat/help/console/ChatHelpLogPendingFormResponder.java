package wbs.apn.chat.help.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlIdAttribute;
import static wbs.utils.web.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlBlockUtils.htmlSpanWrite;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.utils.web.HtmlInputUtils.htmlOptionWrite;
import static wbs.utils.web.HtmlInputUtils.htmlSelectClose;
import static wbs.utils.web.HtmlInputUtils.htmlSelectOpen;
import static wbs.utils.web.HtmlScriptUtils.htmlScriptBlockClose;
import static wbs.utils.web.HtmlScriptUtils.htmlScriptBlockOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;
import static wbs.utils.web.HtmlUtils.htmlLinkWrite;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import wbs.apn.chat.help.model.ChatHelpLogRec;
import wbs.apn.chat.help.model.ChatHelpTemplateRec;
import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.HtmlResponder;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

@PrototypeComponent ("chatHelpLogPendingFormResponder")
public
class ChatHelpLogPendingFormResponder
	extends HtmlResponder {

	// singleton dependencies

	@SingletonDependency
	ChatHelpLogConsoleHelper chatHelpLogHelper;

	@SingletonDependency
	ChatHelpTemplateConsoleHelper chatHelpTemplateHelper;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
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
			chatHelpLogHelper.findRequired (
				requestContext.stuffInteger (
					"chatHelpLogId"));

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

		htmlScriptBlockOpen ();

		formatWriter.writeLineFormat (
			"top.show_inbox (true);");

		formatWriter.writeLineFormat (
			"top.frames ['main'].location = '%j';",
			requestContext.resolveApplicationUrl (
				stringFormat (
					"/chatHelpLog.pending",
					"/%u",
					integerToDecimalString (
						chatHelpLog.getId ()),
					"/chatHelpLog.pending.summary")));

		formatWriter.writeLineFormat (
			"var helpTemplates = new Array ();");

		for (
			ChatHelpTemplateRec chatHelpTemplate
				: chatHelpTemplates
		) {

			formatWriter.writeLineFormat (
				"helpTemplates [%s] = '%j';",
				chatHelpTemplate.getId (),
				chatHelpTemplate.getText ());

		}

		// use template

		formatWriter.writeLineFormatIncreaseIndent (
			"function useTemplate () {");

		formatWriter.writeLineFormat (
			"var templateId = document.getElementById ('template_id');");

		formatWriter.writeLineFormat (
			"var text = document.getElementById ('text');");

		formatWriter.writeLineFormat (
			"if (templateId.value == '') return;");

		formatWriter.writeLineFormat (
			"var template = helpTemplates[templateId.value];");

		formatWriter.writeLineFormat (
			"if (template) text.value = template;");

		formatWriter.writeLineFormatDecreaseIndent (
			"}");

		// show reply

		formatWriter.writeLineFormatIncreaseIndent (
			"function showReply () {");

		formatWriter.writeLineFormatDecreaseIndent (
			"}");

		// show info

		formatWriter.writeLineFormatIncreaseIndent (
			"function showInfo () {");

		formatWriter.writeLineFormatDecreaseIndent (
			"}");

		// script close

		htmlScriptBlockClose ();

	}

	@Override
	public
	void renderHtmlBodyContents () {

		requestContext.flushNotices ();

		// links

		htmlParagraphOpen (
			htmlClassAttribute (
				"links"));

		htmlLinkWrite (
			requestContext.resolveApplicationUrl (
				"/queues/queue.home"),
			"Queues");

		htmlLinkWrite (
			"javascript:top.show_inbox (false);",
			"Close");

		htmlParagraphClose ();

		// heading

		htmlHeadingTwoWrite (
			"Respond to chat help request");

		// form open

		htmlFormOpenPostAction (
			requestContext.resolveLocalUrl (
				"/chatHelpLog.pending.form"));

		// table open

		htmlTableOpenDetails ();

		// request

		htmlTableDetailsRowWrite (
			"Request",
			chatHelpLog.getText ());

		// options

		htmlTableDetailsRowWriteHtml (
			"Options",
			() -> {

			formatWriter.writeLineFormat (
				"<input",
				" type=\"button\"",
				" onclick=\"showReply ()\"",
				" value=\"reply\"",
				">");

			formatWriter.writeLineFormat (
				"<input",
				" type=\"button\"",
				" onclick=\"showInfo ()\"",
				" value=\"change info\"",
				">");

		});

		// template

		htmlTableDetailsRowWriteHtml (
			"Template",
			() -> {

			htmlSelectOpen (
				htmlIdAttribute (
					"template_id"));

			htmlOptionWrite ();

			for (
				ChatHelpTemplateRec chatHelpTemplate
					: chatHelpTemplates
			) {

				htmlOptionWrite (
					integerToDecimalString (
						chatHelpTemplate.getId ()),
					chatHelpTemplate.getCode ());

			}

			htmlSelectClose ();

			formatWriter.writeLineFormat (
				"<input",
				" type=\"button\"",
				" onclick=\"useTemplate ()\"",
				" value=\"ok\"",
				">");

		});

		// reply

		htmlTableRowOpen (
			htmlIdAttribute (
				"replyTextRow"));

		htmlTableHeaderCellWrite (
			"Reply");

		htmlTableCellOpen ();

		formatWriter.writeLineFormat (
			"<textarea",
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
			"></textarea>");

		htmlTableCellClose ();

		htmlTableRowClose ();

		// chars

		htmlTableDetailsRowWriteHtml (
			"Chars",
			() -> htmlSpanWrite (
				"",
				htmlIdAttribute (
					"chars")),
			htmlIdAttribute (
				"replyCharsRow"));

		// actions

		htmlTableDetailsRowWriteHtml (
			"Actions",
			() -> {

			formatWriter.writeLineFormat (
				"<input",
				" id=\"replyButton\"",
				" type=\"submit\"",
				" name=\"reply\"",
				" value=\"send reply\"",
				">");

			formatWriter.writeLineFormat (
				"<input",
				" style=\"display: none\"",
				" id=\"infoButton\"",
				" type=\"submit\"",
				" name=\"info\"",
				" value=\"change info\"",
				">");

			if (requestContext.canContext ("chat.supervisor")) {

				formatWriter.writeLineFormat (
					"<input",
					" id=\"ignoreButton\"",
					" type=\"submit\"",
					" name=\"ignore\"",
					" value=\"ignore request\"",
					">");

			}

		});

		// table close

		htmlTableClose ();

		// form close

		htmlFormClose ();

	}

}
