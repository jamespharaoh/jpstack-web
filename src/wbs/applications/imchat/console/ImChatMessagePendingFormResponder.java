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
import wbs.platform.console.html.HtmlLink;
import wbs.platform.console.html.JqueryScriptRef;
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
	protected
	Set<HtmlLink> myHtmlLinks () {

		return ImmutableSet.<HtmlLink>of (

			HtmlLink.applicationCssStyle (
				"/styles/im-chat.css")

		);

	}

	@Override
	public
	Set<ScriptRef> myScriptRefs () {

		return ImmutableSet.<ScriptRef>of (

			JqueryScriptRef.instance,

			ConsoleApplicationScriptRef.javascript (
				"/js/im-chat.js")

		);

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
	void renderHtmlHeadContents () {

		super.renderHtmlHeadContents ();

		printFormat (
			"<script type=\"text/javascript\">\n");

		printFormat (
			"top.show_inbox (true);\n",
			"top.frames ['main'].location = 'about:blank';\n",
			"window.setTimeout (function () { top.frames ['main'].location = '%j' }, 1);\n",
			summaryUrl);

		printFormat (
			"</script>\n");

	}

	@Override
	public
	void renderHtmlBodyContents () {

		requestContext.flushNotices (out);

		renderLinks ();

		printFormat (
			"<h2>Reply to IM chat</h2>\n");

		renderForm ();

	}

	private
	void renderForm () {

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

		renderBilledTemplate ();
		renderFreeTemplate ();

		for (
			ImChatTemplateRec template
				: templates
		) {

			renderTemplate (
				template);

		}

		printFormat (
			"</table>\n");

		printFormat (
			"</form>\n");

	}

	void renderLinks () {

		printFormat (
			"<p",
			" class=\"links\"",
			">\n");

		printFormat (
			"<a",
			" href=\"%h\">Queues</a>\n",
			requestContext.resolveApplicationUrl (
				"/queues/queue.home"));

		printFormat (
			"<a",
			" href=\"%h\"",
			summaryUrl,
			" target=\"main\"",
			">Summary</a>\n");

		printFormat (
			"<a",
			" href=\"javascript:top.show_inbox (false);\"",
			">Close</a>\n");

		printFormat (
			"</p>\n");

	}

	void renderBilledTemplate () {

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

	void renderFreeTemplate () {

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

	void renderTemplate (
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
