package wbs.platform.exception.console;

import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlAttributeUtils.htmlAttribute;
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

@PrototypeComponent ("exceptionStatusLinePart")
public
class ExceptionStatusLinePart
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
			"#excepRow");

		htmlStyleRuleEntryWrite (
			"display",
			"none");

		htmlStyleRuleEntryWrite (
			"cursor",
			"pointer");

		htmlStyleRuleClose ();

		htmlStyleBlockClose ();

	}

	private
	void renderScriptBlock () {

		// script block open

		htmlScriptBlockOpen ();

		// function open

		formatWriter.writeLineFormatIncreaseIndent (
			"function updateExceptions (numExcep, numExcepFatal) {");

		// variables

		formatWriter.writeLineFormat (
			"var excepCell = document.getElementById ('excepCell');");

		formatWriter.writeLineFormat (
			"var excepRow = document.getElementById ('excepRow');");

		// open fatal exceptions

		formatWriter.writeLineFormatIncreaseIndent (
			"if (numExcepFatal > 0) {");

		// fatal exceptions data

		formatWriter.writeLineFormat (
			"excepCell.firstChild.data =");

		formatWriter.writeLineFormat (
			"  '' + numExcep + ' exceptions (' + numExcepFatal + ' fatal)';");

		formatWriter.writeLineFormatIncreaseIndent (
			"if (excepRow.className == 'hover') {");

		formatWriter.writeLineFormat (
			"excepRow.className = 'alert_hover';");

		formatWriter.writeLineFormatDecreaseIndent (
			"}");

		formatWriter.writeLineFormatIncreaseIndent (
			"if (excepRow.className == '') {");

		formatWriter.writeLineFormat (
			"excepRow.className = 'alert';");

		formatWriter.writeLineFormatDecreaseIndent (
			"}");

		// open non-fatal exceptions

		formatWriter.writeLineFormatDecreaseIncreaseIndent (
			"} else if (numExcep > 0) {");

		// non-fatal exception data

		formatWriter.writeLineFormat (
			"excepCell.firstChild.data =");

		formatWriter.writeLineFormat (
			"  '' + numExcep + ' exceptions';");

		formatWriter.writeLineFormatIncreaseIndent (
			"if (excepRow.className == 'alert_hover') {");

		formatWriter.writeLineFormat (
			"excepRow.className = 'hover';");

		formatWriter.writeLineFormatDecreaseIndent (
			"}");

		formatWriter.writeLineFormatIncreaseIndent (
			"if (excepRow.className == 'alert') {");

		formatWriter.writeLineFormat (
			"excepRow.className = '';");

		formatWriter.writeLineFormatDecreaseIndent (
			"}");

		// close conditional

		formatWriter.writeLineFormatDecreaseIndent (
			"}");

		// show/hide row

		formatWriter.writeLineFormat (
			"showTableRow (excepRow, numExcep > 0 || numExcepFatal > 0);");

		// function close

		formatWriter.writeLineFormatDecreaseIndent (
			"}");

		// script block close

		htmlScriptBlockClose ();

	}

	@Override
	public
	void renderHtmlBodyContent () {

		htmlTableRowOpen (

			htmlIdAttribute (
				"excepRow"),

			htmlAttribute (
				"onmouseover",
				stringFormat (
					"%s; %s",
					"if (this.className=='alert') this.className='alert_hover'",
					"if (this.className=='') this.className='hover'")),

			htmlAttribute (
				"onmouseout",
				stringFormat (
					"%s; %s",
					"if (this.className=='alert_hover') this.className='alert'",
					"if (this.className=='hover') this.className='';")),

			htmlAttribute (
				"onclick",
				stringFormat (
					"top.frames ['main'].location='%j'",
					requestContext.resolveApplicationUrl (
						"/exceptionLogs")))
		);

		htmlTableCellWrite (
			"—",
			htmlIdAttribute (
				"excepCell"));

		htmlTableRowClose ();

	}

}
