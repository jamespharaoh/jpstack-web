package wbs.smsapps.manualresponder.console;

import static wbs.web.utils.HtmlAttributeUtils.htmlIdAttribute;
import static wbs.web.utils.HtmlScriptUtils.htmlScriptBlockClose;
import static wbs.web.utils.HtmlScriptUtils.htmlScriptBlockOpen;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleBlockClose;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleBlockOpen;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleRuleClose;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleRuleEntryWrite;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleRuleOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("manualResponderStatusLinePart")
public
class ManualResponderStatusLinePart
	extends AbstractPagePart {

	@Override
	public
	void renderHtmlHeadContent (
			@NonNull TaskLogger parentTaskLogger) {

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
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

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
