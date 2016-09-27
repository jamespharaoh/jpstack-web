package wbs.platform.status.console;

import static wbs.utils.web.HtmlAttributeUtils.htmlAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlIdAttribute;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.utils.web.HtmlScriptUtils.htmlScriptBlockClose;
import static wbs.utils.web.HtmlScriptUtils.htmlScriptBlockOpen;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleBlockClose;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleBlockOpen;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleRuleClose;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleRuleEntryWrite;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleRuleOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.part.PagePart;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.HtmlResponder;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

@PrototypeComponent ("statusResponder")
public
class StatusResponder
	extends HtmlResponder {

	// singleton dependencies

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	StatusLineManager statusLineManager;

	// state

	List <PagePart> pageParts =
		new ArrayList<> ();

	// details

	@Override
	protected
	Set <ScriptRef> myScriptRefs () {

		return ImmutableSet.<ScriptRef> builder ()

			.add (
				JqueryScriptRef.instance)

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/status.js"))

			.addAll (
				pageParts.stream ()

				.map (
					PagePart::scriptRefs)

				.flatMap (
					Set::stream)

				.iterator ())

			.build ();

	}

	// implementation

	@Override
	protected
	void setup ()
			throws IOException {

		super.setup ();

		for (
			StatusLine statusLine
				: statusLineManager.getStatusLines ()
		) {

			PagePart pagePart =
				statusLine.get ();

			pagePart.setup (
				Collections.emptyMap ());

			pageParts.add (
				pagePart);

		}

	}

	@Override
	protected
	void prepare () {

		super.prepare ();

		pageParts.forEach (
			PagePart::prepare);

	}

	@Override
	protected
	void renderHtmlHeadContents () {

		super.renderHtmlHeadContents ();

		renderStyleBlock ();
		renderScriptBlock ();

		pageParts.forEach (
			PagePart::renderHtmlHeadContent);

	}

	private
	void renderStyleBlock () {

		// style open

		htmlStyleBlockOpen ();

		// time row

		htmlStyleRuleOpen (
			"#timeRow");

		htmlStyleRuleEntryWrite (
			"display",
			"none");

		htmlStyleRuleClose ();

		// notice row

		htmlStyleRuleOpen (
			"#noticeRow");

		htmlStyleRuleEntryWrite (
			"display",
			"none");

		htmlStyleRuleClose ();

		// style close

		htmlStyleBlockClose ();

	}

	private
	void renderScriptBlock () {

		// script open

		htmlScriptBlockOpen ();

		// variables

		formatWriter.writeLineFormat (
			"var statusRequestUrl = '%j';",
			requestContext.resolveApplicationUrl (
				"/status.update"));

		formatWriter.writeLineFormat (
			"var statusRequestTime = 800;");

		// script close

		htmlScriptBlockClose ();

	}

	@Override
	protected
	void renderHtmlBody () {

		formatWriter.writeLineFormat (
			"<body onload=\"statusRequestSchedule ();\">");

		formatWriter.increaseIndent ();

		renderHtmlBodyContents ();

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</body>");

	}

	@Override
	protected
	void renderHtmlBodyContents () {

		// table open

		htmlTableOpenList (

			htmlIdAttribute (
				"statusTable"),

			htmlAttribute (
				"width",
				"100%")

		);

		// heading row

		htmlTableRowOpen ();

		htmlTableHeaderCellWrite (
			"Status",
			htmlIdAttribute (
				"headerCell"));

		htmlTableRowClose ();

		// loading row

		htmlTableRowOpen (
			htmlIdAttribute (
				"loadingRow"));

		htmlTableCellWrite (
			"Loading...",
			htmlIdAttribute (
				"loadingCell"));

		htmlTableRowClose ();

		// notice row

		htmlTableRowOpen (
			htmlIdAttribute (
				"noticeRow"));

		htmlTableCellWrite (
			"—",
			htmlIdAttribute (
				"noticeCell"));

		htmlTableRowClose ();

		// time row

		htmlTableRowOpen (
			htmlIdAttribute (
				"timeRow"));

		htmlTableCellWrite (
			"—",
			htmlIdAttribute (
				"timeCell"));

		htmlTableRowClose ();

		// parts

		pageParts.forEach (
			PagePart::renderHtmlBodyContent);

		// log out row

		htmlTableRowOpen ();

		htmlTableCellOpen ();

		htmlFormOpenPostAction (
			"logoff");

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" value=\"log out\"",
			">");

		htmlFormClose ();

		htmlTableCellClose ();

		htmlTableRowClose ();

		// table close

		htmlTableClose ();

	}

}
