package wbs.apn.chat.contact.console;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlAttributeUtils.htmlIdAttribute;
import static wbs.utils.web.HtmlBlockUtils.htmlHeadingOneWrite;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.utils.web.HtmlInputUtils.htmlOptionWrite;
import static wbs.utils.web.HtmlInputUtils.htmlSelectClose;
import static wbs.utils.web.HtmlInputUtils.htmlSelectOpen;
import static wbs.utils.web.HtmlScriptUtils.htmlScriptBlockClose;
import static wbs.utils.web.HtmlScriptUtils.htmlScriptBlockOpen;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleRuleEntry;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;

import java.util.List;

import wbs.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.help.console.ChatHelpTemplateConsoleHelper;
import wbs.apn.chat.help.model.ChatHelpTemplateRec;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.HtmlResponder;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

@PrototypeComponent ("chatMessagePendingFormResponder")
public
class ChatMessagePendingFormResponder
	extends HtmlResponder {

	// singleton dependencies

	@SingletonDependency
	ChatHelpTemplateConsoleHelper chatHelpTemplateHelper;

	@SingletonDependency
	ChatMessageObjectHelper chatMessageHelper;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// state

	ChatMessageRec chatMessage;

	List <ChatHelpTemplateRec> chatHelpTemplates;

	// implementation

	@Override
	public
	void prepare () {

		chatMessage =
			chatMessageHelper.findRequired (
				requestContext.stuffInteger (
					"chatMessageId"));

		chatHelpTemplates =
			chatHelpTemplateHelper.findByParentAndType (
				chatMessage.getFromUser ().getChat (),
				"reject_message");

	}

	@Override
	public
	void renderHtmlHeadContents () {

		super.renderHtmlHeadContents ();

		// script open

		htmlScriptBlockOpen ();

		// original message

		formatWriter.writeLineFormat (
			"var originalMessage = '%j';",
			chatMessage.getOriginalText ().getText ());

		// edited message

		formatWriter.writeLineFormat (
			"var editedMessage = '%j';",
			chatMessage.getEditedText ().getText ());

		// help templates

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

		// use template function

		formatWriter.writeLineFormatIncreaseIndent (
			"function useTemplate () {");

		formatWriter.writeLineFormat (
			"var templateId = document.getElementById ('templateId');");

		formatWriter.writeLineFormat (
			"var text = document.getElementById ('message');");

		formatWriter.writeLineFormat (
			"if (templateId.value == '') return;");

		formatWriter.writeLineFormat (
			"var template = helpTemplates[templateId.value];");

		formatWriter.writeLineFormat (
			"if (template) text.value = template;");

		formatWriter.writeLineFormatDecreaseIndent (
			"}");

		// show message function

		formatWriter.writeLineFormatIncreaseIndent (
			"function showMessage (message) {");

		formatWriter.writeLineFormat (
			"document.getElementById ('message').value = message;");

		formatWriter.writeLineFormat (
			"document.getElementById ('helpRow').style.display = 'none';");

		formatWriter.writeLineFormat (
			"document.getElementById ('sendButton').style.display = 'inline';");

		if (
			isNotNull (
				requestContext.request (
					"showSendWithoutApproval"))
		) {

			formatWriter.writeLineFormat (
				"document.getElementById ('sendWithoutApprovalButton').style",
				".display = 'inline';");

		}

		formatWriter.writeLineFormat (
			"document.getElementById ('rejectButton').style.display = 'none';");

		formatWriter.writeLineFormatDecreaseIndent (
			"}");

		// show reject function

		formatWriter.writeLineFormatIncreaseIndent (
			"function showReject () {");

		formatWriter.writeLineFormat (
			"document.getElementById ('message').value = '';");

		formatWriter.writeLineFormatIncreaseIndent (
			"try {");

		formatWriter.writeLineFormat (
			"document.getElementById ('helpRow').style.display = 'table-row';");

		formatWriter.writeLineFormatDecreaseIncreaseIndent (
			"} catch (e) {");

		formatWriter.writeLineFormat (
			"document.getElementById ('helpRow').style.display = 'block';");

		formatWriter.writeLineFormatDecreaseIndent (
			"}");

		formatWriter.writeLineFormat (
			"document.getElementById ('sendButton').style.display =\n",
			"    'none';\n");

		if (
			isNotNull (
				requestContext.request (
					"showSendWithoutApproval"))
		) {

			formatWriter.writeLineFormat (
				"document.getElementById ('sendWithoutApprovalButton').style",
				".display = 'none';");

		}

		formatWriter.writeLineFormat (
			"document.getElementById ('rejectButton').style.display = ",
			"'inline';");

		formatWriter.writeLineFormatDecreaseIndent (
			"}");

		// script close

		htmlScriptBlockClose ();

	}

	@Override
	public
	void renderHtmlBodyContents () {

		htmlHeadingOneWrite (
			"Chat message to approve");

		requestContext.flushNotices (
			formatWriter);

		htmlFormOpenPostAction (
			requestContext.resolveApplicationUrl (
				stringFormat (
					"/chatMessage.pending",
					"/%u",
					integerToDecimalString (
						chatMessage.getId ()),
					"/chatMessage.pending.form")));

		formatWriter.writeLineFormat (
			"<input",
			" type=\"hidden\"",
			" name=\"chat_message_id\"",
			" value=\"%h\"",
			integerToDecimalString (
				chatMessage.getId ()),
			">");

		// table open

		htmlTableOpenDetails ();

		// options

		htmlTableDetailsRowWriteHtml (
			"Options",
			() -> {

			formatWriter.writeLineFormat (
				"<input",
				" type=\"button\"",
				" value=\"original message\"",
				" onclick=\"showMessage (originalMessage);\"",
				">");

			formatWriter.writeLineFormat (
				"<input",
				" type=\"button\"",
				" value=\"edited message\"",
				" onclick=\"showMessage (editedMessage);\"",
				">");
		});

		// template

		htmlTableRowOpen (
			htmlIdAttribute (
				"helpRow"),
			htmlStyleRuleEntry (
				"display",
				"none"));

		htmlTableHeaderCellWrite (
			"Template");

		htmlTableCellOpen ();

		htmlSelectOpen (
			htmlIdAttribute (
				"templateId"));

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

		htmlTableCellClose ();

		htmlTableRowClose ();

		// message

		htmlTableDetailsRowWriteHtml (
			"Message",
			() -> formatWriter.writeLineFormat (
				"<textarea",
				" id=\"message\"",
				" name=\"message\"",
				" rows=\"4\"",
				" cols=\"48\"",
				">%h</textarea>",
				ifNull (
					requestContext.parameterOrElse (
						"message",
						() -> chatMessage.getOriginalText ().getText ()))));

		// actions

		htmlTableRowOpen ();

		htmlTableHeaderCellWrite (
			"Actions");

		htmlTableCellOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" id=\"sendButton\"",
			" type=\"submit\"",
			" name=\"send\"",
			" value=\"send as shown\"",
			">");

		if (
			isNotNull (
				requestContext.request (
					"showSendWithoutApproval"))
		) {

			formatWriter.writeLineFormat (
				"<input",
				" id=\"sendWithoutApprovalButton\"",
				" type=\"submit\"",
				" name=\"sendWithoutApproval\"",
				" value=\"send as shown (no warning)\"",
				">");

		}

		formatWriter.writeLineFormat (
			"<input",
			" id=\"rejectButton\"",
			" style=\"display: none\"",
			" type=\"submit\"",
			" name=\"reject\"",
			" value=\"reject and send warning\"",
			">");

		htmlTableCellClose ();

		htmlTableRowClose ();

		// table close

		htmlTableClose ();

		// script

		if (
			optionalIsPresent (
				requestContext.parameter (
					"reject"))
		) {

			htmlScriptBlockOpen ();

			formatWriter.writeLineFormat (
				"showReject ();");

			formatWriter.writeLineFormat (
				"document.getElementById ('message').value = '%j';",
				requestContext.parameterRequired (
					"message"));

			htmlScriptBlockClose ();

		}

		// form close

		htmlScriptBlockClose ();

	}

}
