package wbs.smsapps.manualresponder.console;

import static wbs.utils.etc.LogicUtils.allOf;
import static wbs.utils.etc.LogicUtils.booleanToString;
import static wbs.utils.etc.LogicUtils.ifThenElseEmDash;
import static wbs.utils.etc.LogicUtils.not;
import static wbs.utils.etc.NumberUtils.integerEqualSafe;
import static wbs.utils.etc.NumberUtils.integerNotEqualSafe;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThanZero;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.string.StringUtils.emptyStringIfNull;
import static wbs.web.utils.HtmlAttributeUtils.htmlAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlDataAttribute;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;
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
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;
import static wbs.web.utils.HtmlUtils.htmlEncodeNonBreakingWhitespace;
import static wbs.web.utils.HtmlUtils.htmlLinkWrite;

import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.HtmlResponder;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.currency.logic.CurrencyLogic;

import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.router.logic.RouterLogic;

import wbs.smsapps.manualresponder.model.ManualResponderNumberRec;
import wbs.smsapps.manualresponder.model.ManualResponderRec;
import wbs.smsapps.manualresponder.model.ManualResponderReplyRec;
import wbs.smsapps.manualresponder.model.ManualResponderRequestRec;
import wbs.smsapps.manualresponder.model.ManualResponderTemplateRec;

@PrototypeComponent ("manualResponderRequestPendingFormResponder")
public
class ManualResponderRequestPendingFormResponder
	extends HtmlResponder {

	// singleton dependencies

	@SingletonDependency
	CurrencyLogic currencyLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ManualResponderNumberConsoleHelper manualResponderNumberHelper;

	@SingletonDependency
	ManualResponderRequestConsoleHelper manualResponderRequestHelper;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	RouterLogic routerLogic;

	// state

	ManualResponderRequestRec request;
	ManualResponderNumberRec manualResponderNumber;
	ManualResponderRec manualResponder;
	Set<ManualResponderTemplateRec> templates;
	String summaryUrl;

	boolean canIgnore;
	boolean gotTemplates;
	boolean manager;
	boolean alreadyReplied;

	// details

	@Override
	protected
	Set<HtmlLink> myHtmlLinks () {

		return ImmutableSet.<HtmlLink>of (

			HtmlLink.applicationCssStyle (
				"/styles/manual-responder.css")

		);

	}

	@Override
	public
	Set<ScriptRef> myScriptRefs () {

		return ImmutableSet.<ScriptRef>of (

			JqueryScriptRef.instance,

			ConsoleApplicationScriptRef.javascript (
				"/js/gsm.js"),

			ConsoleApplicationScriptRef.javascript (
				"/js/manual-responder.js")

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

		request =
			manualResponderRequestHelper.findFromContextRequired ();

		manualResponderNumber =
			request.getManualResponderNumber ();

		manualResponder =
			manualResponderNumber.getManualResponder ();

		templates =
			new TreeSet<> ();

		for (
			ManualResponderTemplateRec template
				: manualResponder.getTemplates ()
		) {

			if (template.getRouter () == null) {
				continue;
			}

			if (template.getHidden ()) {
				continue;
			}

			if (template.getRules () != null) {

				String rules =
					template.getRules ();

				Pattern networkIsRulesPattern =
					Pattern.compile (
						"^network is ([0-9]+)$");

				Matcher networkIsRulesMatcher =
					networkIsRulesPattern.matcher (
						rules);

				if (networkIsRulesMatcher.matches ()) {

					long networkId =
						parseIntegerRequired (
							networkIsRulesMatcher.group (1));

					if (
						integerNotEqualSafe (
							manualResponderNumber
								.getNumber ()
								.getNetwork ()
								.getId (),
							networkId)
					) {
						continue;
					}

				}

				Pattern networkIsNotRulesPattern =
					Pattern.compile (
						"^network is not ([0-9]+)$");

				Matcher networkIsNotRulesMatcher =
					networkIsNotRulesPattern.matcher (
						rules);

				if (networkIsNotRulesMatcher.matches ()) {

					long networkId =
						parseIntegerRequired (
							networkIsNotRulesMatcher.group (1));

					if (
						integerEqualSafe (
							manualResponderNumber
								.getNumber ()
								.getNetwork ()
								.getId (),
							networkId)
					) {
						continue;
					}

				}

				if (
					! networkIsRulesMatcher.matches ()
					&& ! networkIsNotRulesMatcher.matches ()
				) {

					throw new RuntimeException (
						"Invalid rules");

				}

			}

			templates.add (
				template);

		}

		summaryUrl =
			requestContext.resolveApplicationUrlFormat (
				"/manualResponderRequest.pending",
				"/%u",
				integerToDecimalString (
					request.getId ()),
				"/manualResponderRequest.pending.summary");

		manager =
			privChecker.canRecursive (
				manualResponder,
				"manage");

		canIgnore =
			manager ||
			manualResponder.getCanIgnore ();

		gotTemplates =
			! templates.isEmpty ();

		Set<ManualResponderReplyRec> manualResponderReplies =
			request.getReplies ();

		alreadyReplied =
			! manualResponderReplies.isEmpty ();

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

		// show relevant frames

		htmlScriptBlockOpen ();

		formatWriter.writeLineFormat (
			"top.show_inbox (true);");

		formatWriter.writeLineFormat (
			"top.frames ['main'].location = 'about:blank';");

		formatWriter.writeLineFormatIncreaseIndent (
			"window.setTimeout (function () {");

		formatWriter.writeLineFormat (
			"top.frames ['main'].location = '%j'\n",
			summaryUrl);

		formatWriter.writeLineFormatDecreaseIndent (
			"}, 1);\n");

		htmlScriptBlockClose ();

	}

	@Override
	public
	void renderHtmlBodyContents (
			@NonNull TaskLogger parentTaskLogger) {

		requestContext.flushNotices (
			formatWriter);

		goLinks ();

		if (request == null) {

			goNotFound ();

		} else if (! request.getPending ()) {

			goNotPending ();

		} else if (

			! privChecker.canRecursive (
				manualResponder,
				"reply")

		) {

			goAccessDenied ();

		} else if (allOf (

			() -> templates.isEmpty (),

			() -> not (
				manualResponder.getCanIgnore ()),

			() -> not (
				privChecker.canRecursive (
					manualResponder,
					"manage"))

		)) {

			goNoTemplates ();

		} else {

			goForm ();

		}

	}

	private
	void goForm () {

		// form open

		htmlFormOpenPostAction (
			requestContext.resolveApplicationUrlFormat (
				"/manualResponderRequest.pending",
				"/%u",
				integerToDecimalString (
					request.getId ()),
				"/manualResponderRequest.pending.form"),
			htmlClassAttribute (
				"manual-responder-request-pending-form"));

		// table open

		htmlTableOpenList (
			htmlStyleRuleEntry (
				"width",
				"100%"));

		// table header

		htmlTableRowOpen ();

		htmlTableHeaderCellWrite (
			"",
			htmlStyleRuleEntry (
				"width",
				"0"));

		htmlTableHeaderCellWrite (
			"Template",
			htmlStyleRuleEntry (
				"width",
				"0"));

		htmlTableHeaderCellWrite (
			"Charge",
			htmlStyleRuleEntry (
				"width",
				"0"));

		htmlTableHeaderCellWrite (
			"Message");

		htmlTableHeaderCellWrite (
			"Send",
			htmlStyleRuleEntry (
				"width",
				"0"));

		htmlTableRowClose ();

		if (alreadyReplied) {
			renderIgnore ();
		}

		int selectedTemplateId = -1;

		Optional <String> templateIdStringOptional =
			requestContext.parameter (
				"template-id");

		if (
			optionalIsPresent (
				templateIdStringOptional)
		) {

			selectedTemplateId =
				Integer.parseInt (
					templateIdStringOptional.get ());

		}

		for (
			ManualResponderTemplateRec template
				: templates
		) {

			goTemplate (
				template,
				template.getId () == selectedTemplateId);

		}

		if (canIgnore && ! alreadyReplied)
			renderIgnore ();

		// table close

		htmlTableClose ();

		// form close

		htmlFormClose ();

	}

	private
	void goTemplate (
			ManualResponderTemplateRec template,
			boolean selected) {

		// table row open

		htmlTableRowOpen (

			htmlClassAttribute (
				"template"),

			htmlDataAttribute (
				"template-id",
				integerToDecimalString (
					template.getId ())),

			htmlDataAttribute (
				"template-mode",
				booleanToString (
					template.getSplitLong (),
					"split",
					"join")),

			htmlDataAttribute (
				"template-min-message-parts",
				integerToDecimalString (
					template.getMinimumMessageParts ())),

			htmlDataAttribute (
				"template-max-messages",
				integerToDecimalString (
					template.getMaximumMessages ())),

			htmlDataAttribute (
				"template-max-for-single-message",
				"160"),

			htmlDataAttribute (
				"template-max-for-message-part",
				integerToDecimalString (
					request
						.getNumber ()
						.getNetwork ()
						.getShortMultipartMessages ()
					? 134l
					: 153l)),

			htmlDataAttribute (
				"template-single",
				booleanToString (
					template.getApplyTemplates (),
					emptyStringIfNull (
						template.getSingleTemplate ()),
					"{message}")),

			htmlDataAttribute (
				"template-first",
				booleanToString (
					template.getApplyTemplates (),
					emptyStringIfNull (
						template.getFirstTemplate ()),
					"{message}")),

			htmlDataAttribute (
				"template-middle",
				booleanToString (
					template.getApplyTemplates (),
					emptyStringIfNull (
						template.getMiddleTemplate ()),
					"{message}")),

			htmlDataAttribute (
				"template-last",
				booleanToString (
					template.getApplyTemplates (),
					emptyStringIfNull (
						template.getLastTemplate ()),
					"{message}"))

		);

		// radio button

		htmlTableCellOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" class=\"template-radio\"",
			" type=\"radio\"",
			" name=\"template-id\"",
			" value=\"%h\"",
			integerToDecimalString (
				template.getId ()),
			selected
				? " checked"
				: "",
			">");

		htmlTableCellClose ();

		// template name

		htmlTableCellWriteHtml (
			htmlEncodeNonBreakingWhitespace (
				template.getName ()));

		// message

		RouteRec route =
			routerLogic.resolveRouter (
				template.getRouter ());

		htmlTableCellWriteHtml (
			htmlEncodeNonBreakingWhitespace (
				ifThenElseEmDash (
					moreThanZero (
						route.getOutCharge ()),
					() -> currencyLogic.formatText (
						route.getCurrency (),
						route.getOutCharge ()))));

		if (template.getCustomisable ()) {

			htmlTableCellOpen ();

			formatWriter.writeLineFormat (
				"<textarea",

				" class=\"template-text\"",
				" style=\"display: none\"",

				" name=\"message-%h\"",
				integerToDecimalString (
					template.getId ()),

				" rows=\"3\"",
				" cols=\"48\"",

				">%h</textarea><br>",
				requestContext.parameterOrDefault (
					"message-" + template.getId (),
					template.getDefaultText ()));

			formatWriter.writeLineFormat (
				"<span",
				" class=\"template-chars\"",
				" style=\"display: none\"",
				"></span>");

			htmlTableCellClose ();

		} else {

			htmlTableCellWriteHtml (
				template.getDefaultText ());

		}

		// send

		htmlTableCellOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" class=\"template-submit\"",
			" type=\"submit\"",
			" value=\"send\"",
			">");

		htmlTableCellClose ();

		// table row close

		htmlTableRowClose ();

	}

	private
	void goNotFound () {

		htmlHeadingTwoWrite (
			"Not found");

		htmlParagraphWriteFormat (
			"The specified request does not exist.");

		htmlParagraphOpen (
			htmlClassAttribute (
				"links"));

		htmlLinkWrite (
			requestContext.resolveApplicationUrl (
				"/queues/queue.home"),
			"Queues");

		htmlParagraphClose ();

	}

	private
	void goNotPending () {

		htmlHeadingTwoWrite (
			"No longer pending");

		htmlParagraphWriteFormat (
			"The specified request does not exist.");

		htmlParagraphOpen (
			htmlClassAttribute (
				"links"));

		htmlLinkWrite (
			requestContext.resolveApplicationUrl (
				"/queues/queue.home"),
			"Queues");

		htmlParagraphClose ();

	}

	private
	void goAccessDenied () {

		htmlHeadingTwoWrite (
			"Access denied");

		htmlParagraphWriteFormat (
			"You do not have permission to reply to this manual responder.");

		htmlParagraphOpen (
			htmlClassAttribute (
				"links"));

		htmlLinkWrite (
			requestContext.resolveContextUrl (
				"/queues/queue.home"),
			"Queues");

		htmlParagraphClose ();

	}

	private
	void goNoTemplates () {

		htmlParagraphWrite (
			"No templates specified for this manual responder",
			htmlClassAttribute (
				"error"));

	}

	private
	void goLinks () {

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

	private
	void renderIgnore () {

		// table row open

		htmlTableRowOpen (
			htmlClassAttribute (
				"template"),
			htmlDataAttribute (
				"template-id",
				"ignore"));

		// radio button

		htmlTableCellOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" class=\"template-radio\"",
			" type=\"radio\"",
			" name=\"template-id\"",
			" value=\"ignore\"",
			">");

		htmlTableCellClose ();

		// blank cells

		htmlTableCellWrite (
			"",
			htmlColumnSpanAttribute (3l));

		// send

		htmlTableCellOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" class=\"template-submit\"",
			" type=\"submit\"",
			" value=\"%h\"",
			alreadyReplied
				? "done sending"
				: "ignore",
			" disabled=\"true\"",
			">");

		htmlTableCellClose ();

		// table row close

		htmlTableRowClose ();

	}

}
