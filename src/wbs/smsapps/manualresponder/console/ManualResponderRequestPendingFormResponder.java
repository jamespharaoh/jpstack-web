package wbs.smsapps.manualresponder.console;

import static wbs.framework.utils.etc.Misc.allOf;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.not;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.HtmlResponder;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.Html;
import static wbs.framework.utils.etc.OptionalUtils.isPresent;
import wbs.platform.currency.logic.CurrencyLogic;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.router.logic.RouterLogic;
import wbs.smsapps.manualresponder.model.ManualResponderNumberObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderNumberRec;
import wbs.smsapps.manualresponder.model.ManualResponderRec;
import wbs.smsapps.manualresponder.model.ManualResponderReplyRec;
import wbs.smsapps.manualresponder.model.ManualResponderRequestObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderRequestRec;
import wbs.smsapps.manualresponder.model.ManualResponderTemplateRec;

@PrototypeComponent ("manualResponderRequestPendingFormResponder")
public
class ManualResponderRequestPendingFormResponder
	extends HtmlResponder {

	// dependencies

	@Inject
	CurrencyLogic currencyLogic;

	@Inject
	ManualResponderNumberObjectHelper manualResponderNumberHelper;

	@Inject
	ManualResponderRequestObjectHelper manualResponderRequestHelper;

	@Inject
	UserPrivChecker privChecker;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
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
	void prepare () {

		super.prepare ();

		request =
			manualResponderRequestHelper.findRequired (
				requestContext.stuffInt (
					"manualResponderRequestId"));

		manualResponderNumber =
			request.getManualResponderNumber ();

		manualResponder =
			manualResponderNumber.getManualResponder ();

		templates =
			new TreeSet<ManualResponderTemplateRec> ();

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

					int networkId =
						Integer.parseInt (
							networkIsRulesMatcher.group (1));

					if (
						notEqual (
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

					int networkId =
						Integer.parseInt (
							networkIsNotRulesMatcher.group (1));

					if (
						equal (
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
			requestContext.resolveApplicationUrl (
				stringFormat (
					"/manualResponderRequest.pending",
					"/%u",
					request.getId (),
					"/manualResponderRequest.pending.summary"));

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
	void renderHtmlHeadContents () {

		super.renderHtmlHeadContents ();

		// show relevant frames

		printFormat (
			"<script language=\"JavaScript\">\n");

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

		requestContext.flushNotices (
			printWriter);

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

		} else if (

			allOf (

				templates.isEmpty (),

				not (
					manualResponder.getCanIgnore ()),

				not (
					privChecker.canRecursive (
						manualResponder,
						"manage")))

		) {

			goNoTemplates ();

		} else {

			goForm ();

		}

	}

	private
	void goForm () {

		printFormat (
			"<form",
			" class=\"manual-responder-request-pending-form\"",
			" action=\"%h\"",
			requestContext.resolveApplicationUrl (
				stringFormat (
					"/manualResponderRequest.pending",
					"/%u",
					request.getId (),
					"/manualResponderRequest.pending.form")),
			" method=\"post\"",
			">\n");

		printFormat (
			"<table",
			" class=\"list\"",
			" style=\"width: 100%%\"",
			">\n");

		printFormat (
			"<tr>\n",
			"<th style=\"width: 0\">&nbsp;</th>\n",
			"<th style=\"width: 0\">Template</th>\n",
			"<th style=\"width: 0\">Charge</th>\n",
			"<th>Message</th>\n",
			"<th style=\"width: 0\">Send</th>\n",
			"</tr>\n");

		if (alreadyReplied)
			renderIgnore ();

		int selectedTemplateId = -1;

		Optional<String> templateIdStringOptional =
			requestContext.parameter (
				"template-id");

		if (
			isPresent (
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

		printFormat (
			"</table>\n");

		printFormat (
			"</form>\n");

	}

	private
	void goTemplate (
			ManualResponderTemplateRec template,
			boolean selected) {

		printFormat (

			"<tr",
			" class=\"template\"",

			" data-template-id=\"%h\"",
			template.getId (),

			" data-template-mode=\"%h\"",
			template.getSplitLong ()
				? "split"
				: "join",

			" data-template-min-message-parts=\"%h\"",
			template.getMinimumMessageParts (),

			" data-template-max-messages=\"%h\"",
			template.getMaximumMessages (),

			" data-template-max-for-single-message=\"%h\"",
			160,

			" data-template-max-for-message-part=\"%h\"",
			request
					.getNumber ()
					.getNetwork ()
					.getShortMultipartMessages ()
				? 134
				: 153,

			" data-template-single=\"%h\"",
			template.getApplyTemplates ()
				? template.getSingleTemplate ()
				: "{message}",

			" data-template-first=\"%h\"",
			template.getApplyTemplates ()
				? template.getFirstTemplate ()
				: "{message}",

			" data-template-middle=\"%h\"",
			template.getApplyTemplates ()
				? template.getMiddleTemplate ()
				: "{message}",

			" data-template-last=\"%h\"",
			template.getApplyTemplates ()
				? template.getLastTemplate ()
				: "{message}",

			">\n");

		printFormat (
			"<td><input",
			" class=\"template-radio\"",
			" type=\"radio\"",
			" name=\"template-id\"",
			" value=\"%h\"",
			template.getId (),
			selected
				? " checked"
				: "",
			"></td>\n");

		printFormat (
			"<td>%s</td>\n",
			Html.nonBreakingWhitespace (
				Html.encode (
					template.getName ())));

		RouteRec route =
			routerLogic.resolveRouter (
				template.getRouter ());

		printFormat (
			"<td>%s</td>\n",
			Html.nonBreakingWhitespace (
				Html.encode (
					route.getOutCharge () > 0
						? currencyLogic.formatText (
							route.getCurrency (),
							Long.valueOf(route.getOutCharge ()))
						: "-")));

		if (template.getCustomisable ()) {

			printFormat (
				"<td><textarea",

				" class=\"template-text\"",
				" style=\"display: none\"",

				" name=\"message-%h\"",
				template.getId (),

				" rows=\"3\"",
				" cols=\"48\"",

				">%h</textarea><br>\n",
				requestContext.parameterOrDefault (
					"message-" + template.getId (),
					template.getDefaultText ()),

				"<span",
				" class=\"template-chars\"",
				" style=\"display: none\"",
				"></span></td>\n");

		} else {

			printFormat (
				"<td>%h</td>\n",
				template.getDefaultText ());

		}

		printFormat (
			"<td><input",
			" class=\"template-submit\"",
			" type=\"submit\"",
			" value=\"send\"",
			"></td>\n");

		printFormat (
			"</tr>\n");

	}

	private
	void goNotFound () {

		printFormat (
			"<h2>Not found</h2>\n");

		printFormat (
			"<p>The specified request does not exist.</p>\n");

		printFormat (
			"<p",
			" class=\"links\"",
			"><a",
			" href=\"%h\"",
			requestContext.resolveApplicationUrl (
				"/queues/queue.home"),
			">Queues</a></p>\n");

	}

	private
	void goNotPending () {

		printFormat (
			"<h2>No longer pending</h2>\n");

		printFormat (
			"<p>The specified request does not exist.</p>\n");

		printFormat (
			"<p",
			" class=\"links\"",
			"><a",
			" href=\"%h\"",
			requestContext.resolveApplicationUrl (
				"/queues/queue.home"),
			">Queues</a></p>\n");

	}

	private
	void goAccessDenied () {

		printFormat (
			"<h2>Access denied</h2>\n");

		printFormat (
			"<p>You do not have permission to reply to this manual ",
			"responder.</p>");

		printFormat (
			"<p",
			" class=\"links\"",
			"><a",
			" href=\"%h\"",
			requestContext.resolveContextUrl (
				"/queues/queue.home"),
			">Queues</a></p>\n");

	}

	private
	void goNoTemplates () {

		printFormat (
			"<p class=\"error\">No templates specified for this manual ",
			"responder.</p>");

	}

	private
	void goLinks () {

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

	private
	void renderIgnore () {

		printFormat (
			"<tr",
			" class=\"template\"",
			" data-template-id=\"ignore\"",
			">\n");

		printFormat (
			"<td><input",
			" class=\"template-radio\"",
			" type=\"radio\"",
			" name=\"template-id\"",
			" value=\"ignore\"",
			"></td>\n");

		printFormat (
			"<td colspan=\"3\">&nbsp;</td>\n");

		printFormat (
			"<td><input",
			" class=\"template-submit\"",
			" type=\"submit\"",
			" value=\"%h\"",
			alreadyReplied
				? "done sending"
				: "ignore",
			" disabled=\"true\"",
			"></td>\n");

		printFormat (
			"</tr>\n");

	}

}
