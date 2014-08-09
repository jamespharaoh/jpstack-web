package wbs.apn.chat.contact.console;

import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;

import wbs.apn.chat.contact.model.ChatMessageObjectHelper;
import wbs.apn.chat.contact.model.ChatMessageRec;
import wbs.apn.chat.help.console.ChatHelpTemplateConsoleHelper;
import wbs.apn.chat.help.model.ChatHelpTemplateRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.console.responder.HtmlResponder;

@PrototypeComponent ("chatMessagePendingFormResponder")
public
class ChatMessagePendingFormResponder
	extends HtmlResponder {

	@Inject
	ChatHelpTemplateConsoleHelper chatHelpTemplateHelper;

	@Inject
	ChatMessageObjectHelper chatMessageHelper;

	@Inject
	ConsoleRequestContext requestContext;

	ChatMessageRec chatMessage;
	List<ChatHelpTemplateRec> chatHelpTemplates;

	@Override
	public
	void prepare () {

		chatMessage =
			chatMessageHelper.find (
				requestContext.stuffInt ("chatMessageId"));

		chatHelpTemplates =
			chatHelpTemplateHelper.findByParentAndType (
				chatMessage.getFromUser ().getChat (),
				"reject_message");

	}

	@Override
	public
	void goHeadStuff () {

		super.goHeadStuff ();

		printFormat (
			"<script language=\"JavaScript\">\n",

			"var originalMessage = '%j';\n",
			chatMessage.getOriginalText ().getText (),

			"var editedMessage = '%j';\n",
			chatMessage.getEditedText ().getText (),

			"var helpTemplates = new Array ();\n");

		for (ChatHelpTemplateRec cht : chatHelpTemplates) {

			printFormat (
				"helpTemplates[%s] = '%j';\n",
				cht.getId (),
				cht.getText ());

		}

		printFormat (
			"function useTemplate () {\n",
			"  var templateId = document.getElementById ('templateId');\n",
			"  var text = document.getElementById ('message');\n",
			"  if (templateId.value == '') return;\n",
			"  var template = helpTemplates[templateId.value];\n",
			"  if (template) text.value = template;\n",
			"}\n",

			"function showMessage (message) {\n",
			"  document.getElementById ('message').value = message;\n",
			"  document.getElementById ('helpRow').style.display = 'none';\n",
			"  document.getElementById ('sendButton').style.display = 'inline';\n");

		if (requestContext.request ("showSendWithoutApproval") != null) {

			printFormat (
				"  document.getElementById ('sendWithoutApprovalButton').style.display = 'inline';\n");

		}

		printFormat (
			"  document.getElementById ('rejectButton').style.display = 'none';\n",
			"}\n",

			"function showReject () {\n",
			"  document.getElementById ('message').value = '';\n",
			"  try {\n",
			"    document.getElementById ('helpRow').style.display = 'table-row';\n",
			"  } catch (e) {\n",
			"    document.getElementById ('helpRow').style.display = 'block';\n",
			"  }\n",
			"  document.getElementById ('sendButton').style.display = 'none';\n");

		if (requestContext.request ("showSendWithoutApproval") != null) {

			printFormat (
				"  document.getElementById ('sendWithoutApprovalButton').style.display = 'none';\n");

		}

		printFormat (
			"  document.getElementById ('rejectButton').style.display = 'inline';\n",
			"}\n",

			"</script>\n");

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<h1>Chat message to approve</h1>");

		requestContext.flushNotices (out);

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

		for (ChatHelpTemplateRec chatHelpTemplate
				: chatHelpTemplates) {

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
				requestContext.parameter ("message"),
				chatMessage.getOriginalText ().getText ()),

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
			isNotNull (
				requestContext.parameter (
					"reject"))
		) {

			printFormat (

				"<script language=\"JavaScript\">\n",
				"showReject ();\n",

				"document.getElementById ('message').value = '%j';\n",
				requestContext.parameter ("message"),

				"</script>\n");

		}

		printFormat (
			"</form>\n");

	}

}
