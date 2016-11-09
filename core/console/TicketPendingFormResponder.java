package wbs.services.ticket.core.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.web.HtmlAttributeUtils.htmlAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlDataAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlIdAttribute;
import static wbs.utils.web.HtmlBlockUtils.htmlHeadingThreeWrite;
import static wbs.utils.web.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.utils.web.HtmlScriptUtils.htmlScriptBlockClose;
import static wbs.utils.web.HtmlScriptUtils.htmlScriptBlockOpen;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleBlockClose;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleBlockOpen;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleRuleEntry;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleRuleWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWriteHtml;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;
import static wbs.utils.web.HtmlUtils.htmlEncodeNonBreakingWhitespace;
import static wbs.utils.web.HtmlUtils.htmlLinkWrite;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import wbs.console.context.ConsoleContextScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.priv.UserPrivChecker;
import wbs.console.responder.HtmlResponder;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.currency.logic.CurrencyLogic;
import wbs.services.ticket.core.model.TicketObjectHelper;
import wbs.services.ticket.core.model.TicketRec;
import wbs.services.ticket.core.model.TicketStateRec;
import wbs.services.ticket.core.model.TicketTemplateRec;

@PrototypeComponent ("ticketPendingFormResponder")
public
class TicketPendingFormResponder
	extends HtmlResponder {

	// singleton dependencies

	@SingletonDependency
	CurrencyLogic currencyLogic;

	@SingletonDependency
	TicketObjectHelper ticketHelper;

	@SingletonDependency
	UserPrivChecker privChecker;

	// state

	TicketRec ticket;
	TicketStateRec ticketState;

	List <TicketTemplateRec> templates;

	String summaryUrl;

	boolean manager;

	// details

	@Override
	public
	Set <ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef> of (

			ConsoleContextScriptRef.javascript (
				"/js/jquery-1.11.2.js"),

			ConsoleContextScriptRef.javascript (
				"/js/TicketPending.js"));

	}

	// implementation

	@Override
	protected
	void prepare () {

		super.prepare ();

		ticket =
			ticketHelper.findRequired (
				requestContext.stuffInteger (
					"ticketId"));

		ticketState =
			ticket.getTicketState ();

		summaryUrl =
			requestContext.resolveApplicationUrlFormat (
				"/ticket.pending",
				"/%u",
				integerToDecimalString (
					ticket.getId ()),
				"/ticket.pending.history");

		ImmutableList.Builder <TicketTemplateRec> templatesBuilder =
			ImmutableList.<TicketTemplateRec> builder ();

		for (
			TicketTemplateRec template
				: ticket.getTicketManager ().getTicketTemplates ()
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

		// script block

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

		// style block

		htmlStyleBlockOpen ();

		htmlStyleRuleWrite (
			".template-chars.error",
			htmlStyleRuleEntry (
				"background-color",
				"darkred"),
			htmlStyleRuleEntry (
				"color",
				"white"),
			htmlStyleRuleEntry (
				"padding-left",
				"10px"),
			htmlStyleRuleEntry (
				"padding-right",
				"10px"));

		htmlStyleBlockClose ();

	}

	@Override
	public
	void renderHtmlBodyContents () {

		requestContext.flushNotices (
			formatWriter);

		// links

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

		// header

		htmlHeadingTwoWrite (
			"Ticket management");

		// form open

		htmlFormOpenPostAction (
			requestContext.resolveApplicationUrlFormat (
				"/ticket.pending",
				"/%u",
				integerToDecimalString (
					ticket.getId ()),
				"/ticket.pending.form"));

		// table open

		htmlTableOpen (
			htmlIdAttribute (
				"templates"),
			htmlClassAttribute (
				"list"),
			htmlStyleRuleEntry (
				"width",
				"100%"));

		// table header

		htmlTableHeaderRowWrite (
			"",
			"Name",
			"New State",
			"Time to queue");

		List <TicketTemplateRec> templatesReversed =
			Lists.reverse (
				templates);

		for (
			TicketTemplateRec template
				: templatesReversed
		) {

			doTemplate (
				template);
		}

		htmlTableClose ();

		addTicketNote ();

		formatWriter.writeLineFormat (
			"<input",
			" class=\"template-submit\"",
			" type=\"submit\"",
			" name=\"send\"",
			" value=\"Send\"",
			">");

		htmlFormClose ();

	}

	void addTicketNote () {

		htmlHeadingThreeWrite (
			"Add new note");

		formatWriter.writeLineFormat (
			"<label for=\"note-text\">",
			"Note text",
			"</label>");

		formatWriter.writeLineFormat (
			"<input",
			" id=\"note-text\"",
			" type=\"textarea\"",
			" name=\"note-text\"",
			">");

	}

	void doTemplate (
			TicketTemplateRec template) {

		htmlTableRowOpen (
			htmlClassAttribute (
				"template"),
			htmlDataAttribute (
				"template",
				integerToDecimalString (
					template.getId ())));

		/*
		if (template.getTicketState()
				.getState().equals (
					ticket.getTicketState().getState())) {

			printFormat (

				"<td><input",

				" id=\"radio-template-%h\"",
				template.getId (),

				" class=\"template-radio\"",

				" type=\"radio\"",

				" name=\"template\"",

				" value=\"%h\"",
				template.getId (),

				"checked",

				"></td>\n");

		}
		else {

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

		}
		*/

		htmlTableCellWriteHtml (
			htmlEncodeNonBreakingWhitespace (
				template.getName ()));

		/*
		printFormat (
			"<td>%h</td>\n",
			template.getTicketState ()
				.getState ()
					.toString ());
		*/

		htmlTableCellOpen ();

		formatWriter.writeLineFormat (
			"<input",

			" id=\"timestamp-%h\"",
			integerToDecimalString (
				template.getId ()),

			" type=\"textarea\"",

			/*
			" name=\"timestamp-%h\"",
			template.getTicketState ().getState ().toString (),
			*/

			" value=\"%h\"",
			integerToDecimalString (
				template.getTicketState ().getMinimum ()),

			">");

		htmlTableCellClose ();

		htmlTableRowClose ();

	}

}

