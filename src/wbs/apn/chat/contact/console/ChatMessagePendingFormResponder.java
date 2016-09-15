package wbs.apn.chat.contact.console;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.string.StringUtils.stringFormat;

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

		printFormat (
			"<script language=\"JavaScript\">\n",

			"var originalMessage = '%j';\n",
			chatMessage.getOriginalText ().getText (),

			"var editedMessage = '%j';\n",
			chatMessage.getEditedText ().getText (),

			"var helpTemplates = new Array ();\n");

		for (
			ChatHelpTemplateRec chatHelpTemplate
				: chatHelpTemplates
		) {

			printFormat (
				"helpTemplates[%s] = '%j';\n",
				chatHelpTemplate.getId (),
				chatHelpTemplate.getText ());

		}

		printFormat (
			"function useTemplate () {\n",
			"  var templateId = document.getElementById ('templateId');\n",
			"  var text = document.getElementById ('message');\n",
			"  if (templateId.value == '') return;\n",
			"  var template = helpTemplates[templateId.value];\n",
			"  if (template) text.value = template;\n",
			"}\n");

		printFormat (
			"function showMessage (message) {\n");

		printFormat (
			"  document.getElementById ('message').value =\n",
			"    message;\n");

		printFormat (
			"  document.getElementById ('helpRow').style.display =\n",
			"    'none';\n");

		printFormat (
			"  document.getElementById ('sendButton').style.display =\n",
			"    'inline';\n");

		if (requestContext.request ("showSendWithoutApproval") != null) {

			printFormat (
				"  document.getElementById ('sendWithoutApprovalButton').style",
					".display =\n",
				"    'inline';\n");

		}

		printFormat (
			"  document.getElementById ('rejectButton').style.display =\n",
			"    'none';\n");

		printFormat (
			"}\n");

		printFormat (
			"function showReject () {\n");

		printFormat (
			"  document.getElementById ('message').value = '';\n");

		printFormat (
			"  try {\n",
			"    document.getElementById ('helpRow').style.display =\n",
			"      'table-row';\n",
			"  } catch (e) {\n",
			"    document.getElementById ('helpRow').style.display =\n",
			"      'block';\n",
			"  }\n");

		printFormat (
			"  document.getElementById ('sendButton').style.display =\n",
			"    'none';\n");

		if (requestContext.request ("showSendWithoutApproval") != null) {

			printFormat (
				"  document.getElementById ('sendWithoutApprovalButton').style",
					".display =\n",
				"    'none';\n");

		}

		printFormat (
			"  document.getElementById ('rejectButton').style.display =\n",
			"    'inline';\n");

		printFormat (
			"}\n");

		printFormat (
			"</script>\n");

	}

	@Override
	public
	void renderHtmlBodyContents () {

		printFormat (
			"<h1>Chat message to approve</h1>");

		requestContext.flushNotices (
			formatWriter);

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveApplicationUrl (
				stringFormat (
					"/chatMessage.pending",
					"/%u",
					chatMessage.getId (),
					"/chatMessage.pending.form")),
			">\n");

		printFormat (
			"<input",
			" type=\"hidden\"",
			" name=\"chat_message_id\"",
			" value=\"%h\"",
			chatMessage.getId (),
			">\n");

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",
			"<th>Options</th>\n",

			"<td><input",
			" type=\"button\"",
			" value=\"original message\"",
			" onclick=\"showMessage (originalMessage);\"",
			">\n",

			"<input",
			" type=\"button\"",
			" value=\"edited message\"",
			" onclick=\"showMessage (editedMessage);\"",
			"></td>\n",

			"</tr>\n");

		printFormat (
			"<tr",
			" id=\"helpRow\"",
			" style=\"display: none\"",
			">\n",

			"<th>Template</th>\n",

			"<td><select id=\"templateId\">\n",
			"<option>\n");

		for (
			ChatHelpTemplateRec chatHelpTemplate
				: chatHelpTemplates
		) {

			printFormat (
				"<option",
				" value=\"%h\"",
				chatHelpTemplate.getId (),
				">%h</option>\n",
				chatHelpTemplate.getCode ());

		}

		printFormat (
			"</select>\n",

			"<input",
			" type=\"button\"",
			" onclick=\"useTemplate ()\"",
			" value=\"ok\"",
			"></td>\n",

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Message</th>\n",

			"<td><textarea",
			" id=\"message\"",
			" name=\"message\"",
			" rows=\"4\"",
			" cols=\"48\"",
			">%h</textarea></td>\n",
			ifNull (
				requestContext.parameterOrElse (
					"message",
					() -> chatMessage.getOriginalText ().getText ())),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Actions</th>\n",

			"<td><input",
			" id=\"sendButton\"",
			" type=\"submit\"",
			" name=\"send\"",
			" value=\"send as shown\"",
			">\n");

		if (
			isNotNull (
				requestContext.request (
					"showSendWithoutApproval"))
		) {

			printFormat (
				"<input",
				" id=\"sendWithoutApprovalButton\"",
				" type=\"submit\"",
				" name=\"sendWithoutApproval\"",
				" value=\"send as shown (no warning)\"",
				">\n");

		}

		printFormat (
			"<input",
			" id=\"rejectButton\"",
			" style=\"display: none\"",
			" type=\"submit\"",
			" name=\"reject\"",
			" value=\"reject and send warning\"",
			"></td>\n",

			"</tr>\n",

			"</table>\n");

		if (
			optionalIsPresent (
				requestContext.parameter (
					"reject"))
		) {

			printFormat (

				"<script language=\"JavaScript\">\n",
				"showReject ();\n",

				"document.getElementById ('message').value = '%j';\n",
				requestContext.parameterRequired (
					"message"),

				"</script>\n");

		}

		printFormat (
			"</form>\n");

	}

}
