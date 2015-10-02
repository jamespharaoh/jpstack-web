package wbs.applications.imchat.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import wbs.applications.imchat.model.ImChatConversationRec;
import wbs.applications.imchat.model.ImChatCustomerRec;
import wbs.applications.imchat.model.ImChatMessageObjectHelper;
import wbs.applications.imchat.model.ImChatMessageRec;
import wbs.applications.imchat.model.ImChatRec;
import wbs.applications.imchat.model.ImChatTemplateRec;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.Html;
import wbs.platform.console.context.ConsoleApplicationScriptRef;
import wbs.platform.console.html.ScriptRef;
import wbs.platform.console.responder.HtmlResponder;
import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.priv.console.PrivChecker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

@PrototypeComponent ("imChatMessagePendingFormResponder")
public
class ImChatMessagePendingFormResponder
	extends HtmlResponder {

	// dependencies

	@Inject
	CurrencyLogic currencyLogic;

	@Inject
	ImChatMessageObjectHelper imChatMessageHelper;

	@Inject
	PrivChecker privChecker;

	// state

	ImChatMessageRec message;
	ImChatConversationRec conversation;
	ImChatCustomerRec customer;
	ImChatRec imChat;
	List<ImChatTemplateRec> templates;

	String summaryUrl;

	boolean manager;

	// details

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>of (

			ConsoleApplicationScriptRef.javascript (
				"/js/jquery-1.11.2.js"),

			ConsoleApplicationScriptRef.javascript (
				"/js/imChatMessagePending.js"));

	}

	// implementation

	@Override
	protected
	void prepare () {

		super.prepare ();

		message =
			imChatMessageHelper.find (
				requestContext.stuffInt ("imChatMessageId"));

		conversation =
			message.getImChatConversation ();

		customer =
			conversation.getImChatCustomer ();

		imChat =
			customer.getImChat ();

		summaryUrl =
			requestContext.resolveApplicationUrl (
				stringFormat (
					"/imChatMessage.pending",
					"/%u",
					message.getId (),
					"/imChatMessage.pending.history"));

		ImmutableList.Builder<ImChatTemplateRec> templatesBuilder =
			ImmutableList.<ImChatTemplateRec>builder ();

		for (
			ImChatTemplateRec template
				: imChat.getTemplates ()
		) {

			if (template.getDeleted ())
				continue;

			templatesBuilder.add (
				template);

		}

		templates =
			templatesBuilder.build ();

	}

	@Override
	public
	void goHeadStuff () {

		super.goHeadStuff ();

		printFormat (
			"<script language=\"JavaScript\">\n");

		printFormat (
			"top.show_inbox (true);\n",
			"top.frames ['main'].location = 'about:blank';\n",
			"window.setTimeout (function () { top.frames ['main'].location = '%j' }, 1);\n",
			summaryUrl);

		printFormat (
			"</script>\n");

		printFormat (
			"<style type=\"text/css\">\n",
			"  .template-chars.error {\n",
			"    background-color: darkred;\n",
			"    color: white;\n",
			"    padding-left: 10px;\n",
			"    padding-right: 10px;\n",
			"  }\n",
			"</style>\n");

	}

	@Override
	public
	void goBodyStuff () {

		requestContext.flushNotices (out);

		printFormat (
			"<p",
			" class=\"links\"",
			">\n",

			"<a",
			" href=\"%h\">Queues</a>\n",
			requestContext.resolveApplicationUrl (
				"/queues/queue.home"),

			"<a",
			" href=\"%h\"",
			summaryUrl,
			" target=\"main\"",
			">Summary</a>\n",

			"<a",
			" href=\"javascript:top.show_inbox (false);\"",
			">Close</a>\n",

			"</p>\n");

		printFormat (
			"<h2>Reply to IM chat</h2>\n");

		printFormat (
			"<form",
			" action=\"%h\"",
			requestContext.resolveApplicationUrl (
				stringFormat (
					"/imChatMessage.pending",
					"/%u",
					message.getId (),
					"/imChatMessage.pending.form")),
			" method=\"post\"",
			">\n");

		printFormat (
			"<table",
			" id=\"templates\"",
			" class=\"list\"",
			" style=\"width: 100%%\"",
			">\n");

		printFormat (
			"<tr>\n",
			"<th>&nbsp;</td>\n",
			"<th>Name</th>\n",
			"<th>Message</th>\n",
			"<th>Action</th>\n",
			"</tr>\n");

		doBilled ();
		doFree ();

		for (
			ImChatTemplateRec template
				: templates
		) {

			doTemplate (
				template);
		}

		printFormat (
			"</table>\n");

		printFormat (
			"</form>\n");

	}

	void doBilled () {

		if (customer.getBalance () < imChat.getMessageCost ())
			return;

		printFormat (
			"<tr",
			" class=\"template\"",
			" data-template=\"bill\"",
			" data-minimum=\"%h\"",
			imChat.getBillMessageMinChars (),
			" data-maximum=\"%h\"",
			imChat.getBillMessageMaxChars (),
			">\n");

		printFormat (
			"<td><input",
			" id=\"radio-template-bill\"",
			" class=\"template-radio\"",
			" type=\"radio\"",
			" name=\"template\"",
			" value=\"bill\"",
			"></td>\n");

		printFormat (
			"<td>Bill&nbsp;%s</td>\n",
			currencyLogic.formatHtml (
				imChat.getCurrency (),
				(long) imChat.getMessageCost ()));

		printFormat (
			"<td",
			" style=\"width: 100%%\"",
			"><textarea",
			" class=\"template-text\"",
			" name=\"message-bill\"",
			" rows=\"3\"",
			" cols=\"48\"",
			" style=\"display: none\"",
			">%h</textarea><br>\n",
			requestContext.parameter (
				"message-bill"),
			"<span",
			" class=\"template-chars\"",
			" style=\"display: none\"",
			"></span></td>\n");

		printFormat (
			"<td><input",
			" class=\"template-submit\"",
			" type=\"submit\"",
			" name=\"send\"",
			" value=\"Send\"",
			" disabled",
			"></td>\n");

		printFormat (
			"</tr>\n");

	}

	void doFree () {

		printFormat (
			"<tr",
			" class=\"template\"",
			" data-template=\"free\"",
			" data-minimum=\"%h\"",
			imChat.getFreeMessageMinChars (),
			" data-maximum=\"%h\"",
			imChat.getFreeMessageMaxChars (),
			">\n");

		printFormat (
			"<td><input",
			" id=\"radio-template-free\"",
			" class=\"template-radio\"",
			" type=\"radio\"",
			" name=\"template\"",
			" value=\"free\"",
			"></td>\n");

		printFormat (
			"<td>Free</td>\n");

		printFormat (
			"<td",
			" style=\"width: 100%%\"",
			"><textarea",
			" class=\"template-text\"",
			" name=\"message-free\"",
			" rows=\"3\"",
			" cols=\"48\"",
			" style=\"display: none\"",
			">%h</textarea><br>\n",
			requestContext.parameter (
				"message-free"),
			"<span",
			" class=\"template-chars\"",
			" style=\"display: none\"",
			"></span></td>\n");


		printFormat (
			"<td><input",
			" class=\"template-submit\"",
			" type=\"submit\"",
			" name=\"send\"",
			" value=\"Send\"",
			" disabled",
			"></td>\n");

		printFormat (
			"</tr>\n");

	}

	void doTemplate (
			ImChatTemplateRec template) {

		printFormat (
			"<tr",
			" class=\"template\"",
			" data-template=\"%h\"",
			template.getId (),
			">\n");

		printFormat (
			"<td><input",
			" id=\"radio-template-%h\"",
			template.getId (),
			" class=\"template-radio\"",
			" type=\"radio\"",
			" name=\"template\"",
			" value=\"%h\"",
			template.getId (),
			"></td>\n");

		printFormat (
			"<td>%s</td>\n",
			Html.nbsp (Html.encode (template.getName ())));

		printFormat (
			"<td>%h</td>\n",
			template.getText ());

		printFormat (
			"<td><input",
			" class=\"template-submit\"",
			" type=\"submit\"",
			" name=\"send\"",
			" value=\"Send\"",
			" disabled",
			"></td>\n");

		printFormat (
			"</td>\n");

	}

}
