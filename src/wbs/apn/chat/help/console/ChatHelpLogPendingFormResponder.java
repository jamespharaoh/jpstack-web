package wbs.apn.chat.help.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlIdAttribute;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlBlockUtils.htmlSpanWrite;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlInputUtils.htmlOptionWrite;
import static wbs.web.utils.HtmlInputUtils.htmlSelectClose;
import static wbs.web.utils.HtmlInputUtils.htmlSelectOpen;
import static wbs.web.utils.HtmlScriptUtils.htmlScriptBlockClose;
import static wbs.web.utils.HtmlScriptUtils.htmlScriptBlockOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;
import static wbs.web.utils.HtmlUtils.htmlLinkWrite;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ConsoleHtmlResponder;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

import wbs.apn.chat.help.model.ChatHelpLogRec;
import wbs.apn.chat.help.model.ChatHelpTemplateRec;

@PrototypeComponent ("chatHelpLogPendingFormResponder")
public
class ChatHelpLogPendingFormResponder
	extends ConsoleHtmlResponder {

	// singleton dependencies

	@SingletonDependency
	ChatHelpLogConsoleHelper chatHelpLogHelper;

	@SingletonDependency
	ChatHelpTemplateConsoleHelper chatHelpTemplateHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// state

	ChatHelpLogRec chatHelpLog;

	List <ChatHelpTemplateRec> chatHelpTemplates;

	// details

	@Override
	protected
	Set <ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef> builder ()

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
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			chatHelpLog =
				chatHelpLogHelper.findFromContextRequired (
					transaction);

			chatHelpTemplates =
				chatHelpTemplateHelper.findByParentAndType (
					transaction,
					chatHelpLog.getChatUser ().getChat (),
					"help");

			Collections.sort (
				chatHelpTemplates);

		}

	}

	@Override
	public
	void renderHtmlHeadContents (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlHeadContents");

		) {

			super.renderHtmlHeadContents (
				transaction,
				formatWriter);

			htmlScriptBlockOpen (
				formatWriter);

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
					integerToDecimalString (
						chatHelpTemplate.getId ()),
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

			htmlScriptBlockClose (
				formatWriter);

		}

	}

	@Override
	public
	void renderHtmlBodyContents (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContents");

		) {

			requestContext.flushNotices (
				formatWriter);

			// links

			htmlParagraphOpen (
				formatWriter,
				htmlClassAttribute (
					"links"));

			htmlLinkWrite (
				formatWriter,
				requestContext.resolveApplicationUrl (
					"/queues/queue.home"),
				"Queues");

			htmlLinkWrite (
				formatWriter,
				"javascript:top.show_inbox (false);",
				"Close");

			htmlParagraphClose (
				formatWriter);

			// heading

			htmlHeadingTwoWrite (
				formatWriter,
				"Respond to chat help request");

			// form open

			htmlFormOpenPostAction (
				formatWriter,
				requestContext.resolveLocalUrl (
					"/chatHelpLog.pending.form"));

			// table open

			htmlTableOpenDetails (
				formatWriter);

			// request

			htmlTableDetailsRowWrite (
				formatWriter,
				"Request",
				chatHelpLog.getText ());

			// options

			htmlTableDetailsRowWriteHtml (
				formatWriter,
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
				formatWriter,
				"Template",
				() -> {

				htmlSelectOpen (
					formatWriter,
					htmlIdAttribute (
						"template_id"));

				htmlOptionWrite (
					formatWriter);

				for (
					ChatHelpTemplateRec chatHelpTemplate
						: chatHelpTemplates
				) {

					htmlOptionWrite (
						formatWriter,
						integerToDecimalString (
							chatHelpTemplate.getId ()),
						chatHelpTemplate.getCode ());

				}

				htmlSelectClose (
					formatWriter);

				formatWriter.writeLineFormat (
					"<input",
					" type=\"button\"",
					" onclick=\"useTemplate ()\"",
					" value=\"ok\"",
					">");

			});

			// reply

			htmlTableRowOpen (
				formatWriter,
				htmlIdAttribute (
					"replyTextRow"));

			htmlTableHeaderCellWrite (
				formatWriter,
				"Reply");

			htmlTableCellOpen (
				formatWriter);

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

			htmlTableCellClose (
				formatWriter);

			htmlTableRowClose (
				formatWriter);

			// chars

			htmlTableDetailsRowWriteHtml (
				formatWriter,
				"Chars",
				() -> htmlSpanWrite (
					formatWriter,
					"",
					htmlIdAttribute (
						"chars")),
				htmlIdAttribute (
					"replyCharsRow"));

			// actions

			htmlTableDetailsRowWriteHtml (
				formatWriter,
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

			htmlTableClose (
				formatWriter);

			// form close

			htmlFormClose (
				formatWriter);

		}

	}

}
