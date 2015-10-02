package wbs.smsapps.manualresponder.console;

import static wbs.framework.utils.etc.Misc.allOf;
import static wbs.framework.utils.etc.Misc.not;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.json.simple.JSONValue;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.context.ConsoleApplicationScriptRef;
import wbs.platform.console.html.ScriptRef;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.console.responder.HtmlResponder;
import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.priv.console.PrivChecker;
import wbs.sms.gsm.Gsm;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.router.logic.RouterLogic;
import wbs.smsapps.manualresponder.model.ManualResponderNumberObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderRec;
import wbs.smsapps.manualresponder.model.ManualResponderReplyRec;
import wbs.smsapps.manualresponder.model.ManualResponderRequestObjectHelper;
import wbs.smsapps.manualresponder.model.ManualResponderRequestRec;
import wbs.smsapps.manualresponder.model.ManualResponderTemplateRec;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

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
	PrivChecker privChecker;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	RouterLogic routerLogic;

	// state

	ManualResponderRequestRec manualResponderRequest;
	ManualResponderRec manualResponder;
	NumberRec number;
	Set<ManualResponderTemplateRec> manualResponderTemplates;
	String summaryUrl;

	boolean canIgnore;
	boolean gotTemplates;
	boolean manager;
	boolean alreadyReplied;

	// details

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/DOM.js"))

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/gsm.js"))

			.build ();

	}

	// implementation

	@Override
	protected
	void prepare () {

		super.prepare ();

		manualResponderRequest =
			manualResponderRequestHelper.find (
				requestContext.stuffInt ("manualResponderRequestId"));

		manualResponder =
			manualResponderRequest.getManualResponder ();

		number =
			manualResponderRequest.getNumber ();

		manualResponderTemplates =
			new TreeSet<ManualResponderTemplateRec> ();

		for (
			ManualResponderTemplateRec manualResponderTemplate
				: manualResponder.getTemplates ()
		) {

			if (manualResponderTemplate.getRouter () == null)
				continue;

			if (manualResponderTemplate.getRules () != null) {

				String rules =
					manualResponderTemplate.getRules ();

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

					if (number.getNetwork ().getId () != networkId)
						continue;

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

					if (number.getNetwork ().getId () == networkId)
						continue;

				}

				if (
					! networkIsRulesMatcher.matches ()
					&& ! networkIsNotRulesMatcher.matches ()
				) {

					throw new RuntimeException (
						"Invalid rules");

				}

			}

			manualResponderTemplates.add (
				manualResponderTemplate);

		}

		summaryUrl =
			requestContext.resolveApplicationUrl (
				stringFormat (
					"/manualResponderRequest.pending",
					"/%u",
					manualResponderRequest.getId (),
					"/manualResponderRequest.pending.summary"));

		manager =
			privChecker.can (
				manualResponder,
				"manage");

		canIgnore =
			manager ||
			manualResponder.getCanIgnore ();

		gotTemplates =
			! manualResponderTemplates.isEmpty ();

		Set<ManualResponderReplyRec> manualResponderReplies =
			manualResponderRequest.getReplies ();

		alreadyReplied =
			! manualResponderReplies.isEmpty ();

	}

	@Override
	public
	void goHeadStuff () {

		super.goHeadStuff ();

		printFormat (
			"<script language=\"JavaScript\">\n");

		// template data

		printFormat (
			"template_ids = [ ");

		boolean comma = false;

		for (ManualResponderTemplateRec template
				: manualResponderTemplates) {

			if (comma) {
				printFormat (
					", ");
			} else {
				comma = true;
			}

			printFormat (
				"%s",
				template.getId ());

		}

		printFormat (
			" ]\n");

		// function to update form state

		printFormat (
			"function form_magic () {\n",
			"  for (var i = 0; i < template_ids.length; i++) {\n",
			"    var id = template_ids [i];\n",
			"    var radio = document.getElementById ('template_' + id);\n",
			"    var message = document.getElementById ('message_' + id);\n",
			"    var row = document.getElementById ('row_' + id);\n",
			"    var submit = document.getElementById ('submit_' + id);\n",
			"    if (radio && message) message.disabled = ! radio.checked;\n",
			"    if (radio && submit) submit.disabled = ! radio.checked;\n",
			"    if (radio && row) row.className = radio.checked ? 'selected' : '';\n",
			"  }");

		if (alreadyReplied || canIgnore) {

			printFormat (
				"  var radio = document.getElementById ('template_ignore');\n",
				"  var row = document.getElementById ('row_ignore');\n",
				"  var submit = document.getElementById ('submit_ignore');\n",
				"  if (radio && row) row.className = radio.checked ? 'selected' : '';\n",
				"  if (radio && submit) submit.disabled = ! radio.checked;\n");

		}

		printFormat (
			"}\n");

		// show relevant frames

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

		if (manualResponderRequest == null) {

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

			return;

		}

		if (! manualResponderRequest.getPending ()) {

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

			return;

		}

		if (! privChecker.can (
				manualResponderRequest.getManualResponder (),
				"reply")) {

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

			return;

		}

		if (

			allOf (

				manualResponderTemplates.isEmpty (),

				not (
					manualResponderRequest
						.getManualResponder ()
						.getCanIgnore ()),

				not (
					privChecker.can (
						manualResponderRequest
							.getManualResponder (),
						"manage")))

		) {

			printFormat (
				"<p class=\"error\">No templates specified for this manual ",
				"responder.</p>");

			return;

		}

		printFormat (
			"<form",
			" action=\"%h\"",
			requestContext.resolveApplicationUrl (
				stringFormat (
					"/manualResponderRequest.pending",
					"/%u",
					manualResponderRequest.getId (),
					"/manualResponderRequest.pending.form")),
			" method=\"post\"",
			">\n");

		printFormat (
			"<input",
			" type=\"hidden\"",
			" name=\"request_id\"",
			" value=\"%h\"",
			manualResponderRequest.getId (),
			">");

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>&nbsp;</th>\n",
			"<th>Template</th>\n",
			"<th>Charge</th>\n",
			"<th>Message</th>\n",
			"<th>Chars</th>\n",
			"<th>Send</th>\n",
			"</tr>\n");

		if (alreadyReplied)
			doIgnore ();

		int selectedTemplateId = -1;

		String templateIdString =
			requestContext.parameter ("template_id");

		if (templateIdString != null) {

			selectedTemplateId =
				Integer.parseInt (templateIdString);

		}

		for (ManualResponderTemplateRec template
				: manualResponderTemplates) {

			printFormat (
				"<tr",
				" id=\"row_%h\"",
				template.getId (),
				" onclick=\"%h\"",
				stringFormat (
					"document.getElementById ('template_%j').checked = true; ",
					template.getId (),
					"form_magic ()"),
				">\n");

			printFormat (
				"<td><input",
				" id=\"template_%h\"",
				template.getId (),
				" type=\"radio\"",
				" name=\"template_id\"",
				" value=\"%h\"",
				template.getId (),
				" onclick=\"form_magic ()\"",
				template.getId () == selectedTemplateId
					? " checked"
					: "",
				"></td>\n");

			printFormat (
				"<td>%h</td>\n",
				template.getName ());

			RouteRec route =
				routerLogic.resolveRouter (
					template.getRouter ());

			printFormat (
				"<td>%h</td>\n",
				route.getOutCharge () > 0
					? currencyLogic.formatText (
						route.getCurrency (),
						Long.valueOf(route.getOutCharge ()))
					: "-");

			if (
				template.getCustomisable ()
			) {

				int fixedLength;

				if (template.getSingleTemplate () != null) {

					String fixedText =
						template.getSingleTemplate ().replace (
							"{message}",
							"");

					fixedLength =
						Gsm.length (
							fixedText);

				} else {

					fixedLength = 0;

				}

				String charCountOptions =
					JSONValue.toJSONString (
						ImmutableMap.builder ()

					.put (
						"fixedLength",
						fixedLength)

					.put (
						"maxForSingleMessage",
						160)

					.put (
						"maxForMessagePart",
						manualResponderRequest
								.getNumber ()
								.getNetwork ()
								.getShortMultipartMessages ()
							? 134
							: 153)

					.build ()

				);

				String charCountScript =
					stringFormat (
						"gsmCharCountMultiple (this, %s, %s);",
						stringFormat (
							"document.getElementById ('chars_%j')",
							template.getId ()),
						charCountOptions);

				printFormat (
					"<td><textarea",

					" id=\"message_%h\"",
					template.getId (),

					" name=\"message_%h\"",
					template.getId (),

					" onkeyup=\"%h\"",
					charCountScript,

					" onfocus\"%h\"",
					charCountScript,

					" onclick=\"%h\"",
					stringFormat (
						"e = new MyEvent (event); ",
						"e.stopPropagation ()"),

					" rows=\"3\"",
					" cols=\"48\"",

					template.getId () != selectedTemplateId
						? " disabled"
						: "",

					">%h</textarea></td>\n",
					requestContext.parameter (
						"message_" + template.getId (),
						template.getDefaultText ()));

			} else {

				printFormat (
					"<td>%h</td>\n",
					template.getDefaultText ());

			}

			printFormat (
				"<td",
				" style=\"text-align: center\"",
				"><span",
				" id=\"chars_%h\"",
				template.getId (),
				">&nbsp;</span></td>\n");

			printFormat (
				"<td><input",
				" id=\"submit_%h\"",
				template.getId (),
				" type=\"submit\"",
				" value=\"send\"",
				"></td>\n");

			printFormat (
				"</tr>\n");

		}

		if (canIgnore && ! alreadyReplied)
			doIgnore ();

		printFormat (
			"</table>\n");

		printFormat (
			"</form>\n");

		printFormat (
			"<script language=\"JavaScript\">\n",
			"form_magic ();\n",
			"</script>\n");

	}

	private
	void doIgnore () {

		printFormat (
			"<tr",
			" id=\"row_ignore\"",
			" onclick=\"%h\"",
			stringFormat (
				"%s; %s",
				"document.getElementById ('template_ignore').checked = true",
				"form_magic ()"),
			">\n",

			"<td><input",
			" id=\"template_ignore\"",
			" type=\"radio\"",
			" name=\"template_id\"",
			" value=\"ignore\"",
			" onclick=\"form_magic ()\"",
			"></td>\n",

			"<td colspan=\"4\">&nbsp;</td>\n",

			"<td><input",
			" id=\"submit_ignore\"",
			" type=\"submit\"",
			" value=\"%h\"",
			alreadyReplied
				? "done sending"
				: "ignore",
			" disabled=\"true\"",
			"></td>\n",

			"</tr>\n");

	}

}
