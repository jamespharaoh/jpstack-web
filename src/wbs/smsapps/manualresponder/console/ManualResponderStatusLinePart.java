package wbs.smsapps.manualresponder.console;

import static wbs.utils.web.HtmlAttributeUtils.htmlIdAttribute;
import static wbs.utils.web.HtmlScriptUtils.htmlScriptBlockClose;
import static wbs.utils.web.HtmlScriptUtils.htmlScriptBlockOpen;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleBlockClose;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleBlockOpen;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleRuleClose;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleRuleEntryWrite;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleRuleOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;

@PrototypeComponent ("manualResponderStatusLinePart")
public
class ManualResponderStatusLinePart
	extends AbstractPagePart {

	@Override
	public
	void renderHtmlHeadContent () {

		renderStyleBlock ();
		renderScriptBlock ();

	}

	private
	void renderStyleBlock () {

		htmlStyleBlockOpen ();

		htmlStyleRuleOpen (
			"#manualResponderRow");

		htmlStyleRuleEntryWrite (
			"display",
			"none");

		htmlStyleRuleClose ();

		htmlStyleBlockClose ();

	}

	private
	void renderScriptBlock () {

		htmlScriptBlockOpen ();

		formatWriter.writeLineFormatIncreaseIndent (
			"function updateManualResponder (numToday, numThisHour) {");

		formatWriter.writeLineFormat (
			"var cell = $('#manualResponderCell');");

		formatWriter.writeLineFormat (
			"var row = $('#manualResponderRow');");

		formatWriter.writeLineFormatIncreaseIndent (
			"cell.text ('Messages answered: ' + [");

		formatWriter.writeLineFormat (
			"String (numToday) + ' today',");

		formatWriter.writeLineFormat (
			"String (numThisHour) + ' this hour',");

		formatWriter.writeLineFormatDecreaseIndent (
			"].join (', '));");

		formatWriter.writeLineFormat (
			"showTableRow (row [0], numToday > 0 || numThisHour > 0);");

		formatWriter.writeLineFormatDecreaseIndent (
			"}");

		htmlScriptBlockClose ();

	}

	@Override
	public
	void renderHtmlBodyContent () {

		htmlTableRowOpen (
			htmlIdAttribute (
				"manualResponderRow"));

		htmlTableCellWrite (
			"—",
			htmlIdAttribute (
				"manualResponderCell"));

		htmlTableRowClose ();

	}

}
