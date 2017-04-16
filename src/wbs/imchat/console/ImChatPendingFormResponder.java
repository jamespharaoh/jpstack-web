package wbs.imchat.console;

import static wbs.utils.etc.Misc.lessThan;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlAttributeUtils.htmlAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlDataAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlIdAttribute;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlScriptUtils.htmlScriptBlockClose;
import static wbs.web.utils.HtmlScriptUtils.htmlScriptBlockOpen;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleRuleEntry;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;
import static wbs.web.utils.HtmlUtils.htmlEncodeNonBreakingWhitespace;
import static wbs.web.utils.HtmlUtils.htmlLinkWrite;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.priv.UserPrivChecker;
import wbs.console.responder.HtmlResponder;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.currency.logic.CurrencyLogic;

import wbs.imchat.model.ImChatConversationRec;
import wbs.imchat.model.ImChatCustomerRec;
import wbs.imchat.model.ImChatMessageRec;
import wbs.imchat.model.ImChatRec;
import wbs.imchat.model.ImChatTemplateRec;

@PrototypeComponent ("imChatPendingFormResponder")
public
class ImChatPendingFormResponder
	extends HtmlResponder {

	// dependencies

	@SingletonDependency
	CurrencyLogic currencyLogic;

	@SingletonDependency
	ImChatMessageConsoleHelper imChatMessageHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserPrivChecker privChecker;

	// state

	ImChatMessageRec message;
	ImChatConversationRec conversation;
	ImChatCustomerRec customer;
	ImChatRec imChat;
	List <ImChatTemplateRec> templates;

	String summaryUrl;

	boolean manager;

	// details

	@Override
	protected
	Set <HtmlLink> myHtmlLinks () {

		return ImmutableSet.<HtmlLink>of (

			HtmlLink.applicationCssStyle (
				"/styles/im-chat.css")

		);

	}

	@Override
	public
	Set <ScriptRef> myScriptRefs () {

		return ImmutableSet.<ScriptRef>of (

			JqueryScriptRef.instance,

			ConsoleApplicationScriptRef.javascript (
				"/js/im-chat.js")

		);

	}

	// implementation

	@Override
	protected
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"prepare");

		super.prepare (
			taskLogger);

		message =
			imChatMessageHelper.findFromContextRequired ();

		conversation =
			message.getImChatConversation ();

		customer =
			conversation.getImChatCustomer ();

		imChat =
			customer.getImChat ();

		summaryUrl =
			requestContext.resolveApplicationUrlFormat (
				"/imChat.pending",
				"/%u",
				integerToDecimalString (
					message.getId ()),
				"/imChat.pending.summary");

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
	void renderHtmlHeadContents (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderHtmlHeadContents");

		super.renderHtmlHeadContents (
			taskLogger);

		htmlScriptBlockOpen ();

		formatWriter.writeLineFormat (
			"top.show_inbox (true);");

		formatWriter.writeLineFormat (
			"top.frames ['main'].location = 'about:blank';");

		formatWriter.writeLineFormatIncreaseIndent (
			"window.setTimeout (function () {");

		formatWriter.writeLineFormat (
			"top.frames ['main'].location = '%j';",
			summaryUrl);

		formatWriter.writeLineFormatDecreaseIndent (
			"}, 1);");

		htmlScriptBlockClose ();

	}

	@Override
	public
	void renderHtmlBodyContents (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderHtmlBodyContents");

		requestContext.flushNotices (
			formatWriter);

		renderLinks ();

		htmlHeadingTwoWrite (
			"Reply to IM chat");

		renderForm (
			taskLogger);

	}

	private
	void renderForm (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderForm");

		// form open

		htmlFormOpenPostAction (
			requestContext.resolveApplicationUrlFormat (
				"/imChat.pending",
				"/%u",
				integerToDecimalString (
					message.getId ()),
				"/imChat.pending.form"));

		// table open

		htmlTableOpenList (
			htmlIdAttribute (
				"templates"));

		// table headers

		htmlTableHeaderRowWrite (
			"",
			"Name",
			"Message",
			"Action");

		renderBilledTemplate ();
		renderFreeTemplate ();

		for (
			ImChatTemplateRec template
				: templates
		) {

			renderTemplate (
				template);

		}

		renderIgnore (
			taskLogger);

		// table close

		htmlTableClose ();

		// form close

		htmlFormClose ();

	}

	void renderLinks () {

		htmlParagraphOpen (
			htmlClassAttribute (
				"links"));

		htmlLinkWrite (
			requestContext.resolveApplicationUrl (
				"/queues/queue.home"),
			"Queues");

		htmlLinkWrite (
			summaryUrl,
			"Summary",
			htmlAttribute (
				"target",
				"main"));

		htmlLinkWrite (
			"javascript:top.show_inbox (false);",
			"Close");

		htmlParagraphClose ();

	}

	void renderBilledTemplate () {

		if (

			! imChat.getBillMessageEnabled ()

			|| lessThan (
				customer.getBalance (),
				imChat.getMessageCost ())

		) {
			return;
		}

		// table row open

		htmlTableRowOpen (
			htmlClassAttribute (
				"template"),
			htmlDataAttribute (
				"template",
				"bill"),
			htmlDataAttribute (
				"minimum",
				integerToDecimalString (
					imChat.getBillMessageMinChars ())),
			htmlDataAttribute (
				"maximum",
				integerToDecimalString (
					imChat.getBillMessageMaxChars ())));

		// radio button

		htmlTableCellOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" id=\"radio-template-bill\"",
			" class=\"template-radio\"",
			" type=\"radio\"",
			" name=\"template\"",
			" value=\"bill\"",
			">");

		htmlTableCellClose ();

		// template name

		htmlTableCellWriteHtml (
			stringFormat (
				"Bill&nbsp;%s",
				currencyLogic.formatHtml (
					imChat.getCreditCurrency (),
					imChat.getMessageCost ())));

		// message

		htmlTableCellOpen (
			htmlStyleRuleEntry (
				"width",
				"100%"));

		formatWriter.writeLineFormat (
			"<textarea",
			" class=\"template-text\"",
			" name=\"message-bill\"",
			" rows=\"3\"",
			" cols=\"48\"",
			" style=\"display: none\"",
			">%h</textarea><br>",
			requestContext.parameterOrEmptyString (
				"message-bill"));

		formatWriter.writeLineFormat (
			"<span",
			" class=\"template-chars\"",
			" style=\"display: none\"",
			"></span>");

		htmlTableCellClose ();

		// send

		htmlTableCellOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" class=\"template-submit\"",
			" type=\"submit\"",
			" name=\"send\"",
			" value=\"Send\"",
			" disabled",
			">");

		htmlTableCellClose ();

		// table row close

		htmlTableRowClose ();

	}

	void renderFreeTemplate () {

		if (! imChat.getFreeMessageEnabled ()) {
			return;
		}

		// table row open

		htmlTableRowOpen (
			htmlClassAttribute (
				"template"),
			htmlDataAttribute (
				"template",
				"free"),
			htmlDataAttribute (
				"minimum",
				integerToDecimalString (
					imChat.getFreeMessageMinChars ())),
			htmlDataAttribute (
				"maximum",
				integerToDecimalString (
					imChat.getFreeMessageMaxChars ())));

		// radio button

		htmlTableCellOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" id=\"radio-template-free\"",
			" class=\"template-radio\"",
			" type=\"radio\"",
			" name=\"template\"",
			" value=\"free\"",
			">");

		htmlTableCellClose ();

		// template name

		htmlTableCellWrite (
			"Free");

		// message

		htmlTableCellOpen (
			htmlStyleRuleEntry (
				"width",
				"100%"));

		formatWriter.writeLineFormat (
			"<textarea",
			" class=\"template-text\"",
			" name=\"message-free\"",
			" rows=\"3\"",
			" cols=\"48\"",
			" style=\"display: none\"",
			">%h</textarea><br>",
			requestContext.parameterOrEmptyString (
				"message-free"));

		formatWriter.writeLineFormat (
			"<span",
			" class=\"template-chars\"",
			" style=\"display: none\"",
			"></span>");

		htmlTableCellClose ();

		// send

		htmlTableCellOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" class=\"template-submit\"",
			" type=\"submit\"",
			" name=\"send\"",
			" value=\"Send\"",
			" disabled",
			">");

		htmlTableCellClose ();

		// table row close

		htmlTableRowClose ();

	}

	void renderTemplate (
			ImChatTemplateRec template) {

		// table row open

		htmlTableRowOpen (
			htmlClassAttribute (
				"template"),
			htmlDataAttribute (
				"template",
				integerToDecimalString (
					template.getId ())));

		// radio button

		htmlTableCellOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" id=\"radio-template-%h\"",
			integerToDecimalString (
				template.getId ()),
			" class=\"template-radio\"",
			" type=\"radio\"",
			" name=\"template\"",
			" value=\"%h\"",
			integerToDecimalString (
				template.getId ()),
			">");

		htmlTableCellClose ();

		// template name

		htmlTableCellWriteHtml (
			htmlEncodeNonBreakingWhitespace (
				template.getName ()));

		// message

		htmlTableCellWrite (
			template.getText ());

		// send

		htmlTableCellOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" class=\"template-submit\"",
			" type=\"submit\"",
			" name=\"send\"",
			" value=\"Send\"",
			" disabled",
			">");

		htmlTableCellClose ();

		// table row close

		htmlTableRowClose ();

	}

	void renderIgnore (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderIgnore");

		if (
			! privChecker.canRecursive (
				taskLogger,
				imChat,
				"supervisor")
		) {
			return;
		}

		// table row open

		htmlTableRowOpen (
			htmlClassAttribute (
				"template"));

		// radio button

		htmlTableCellOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" id=\"radio-template-ignore\"",
			" class=\"template-radio\"",
			" type=\"radio\"",
			" name=\"template\"",
			" value=\"ignore\"",
			">");

		htmlTableCellClose ();

		// template name

		htmlTableCellWrite (
			"Ignore");

		// message

		htmlTableCellWrite (
			"");

		// send

		htmlTableCellOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" class=\"template-submit\"",
			" type=\"submit\"",
			" name=\"ignore\"",
			" value=\"Ignore\"",
			" disabled",
			">");

		htmlTableCellClose ();

		// table row close

		htmlTableRowClose ();

	}

}
