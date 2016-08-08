package wbs.platform.exception.console;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("exceptionStatusLinePart")
public
class ExceptionStatusLinePart
	extends AbstractPagePart {

	@Override
	public
	void renderHtmlHeadContent () {

		printFormat (
			"<style type=\"text/css\">\n",
			"#excepRow { display: none; cursor: pointer; }\n",
			"</style>\n");

		printFormat (
			"<script type=\"text/javascript\">\n",
			"function updateExceptions (numExcep, numExcepFatal) {\n",
			"  var excepCell = document.getElementById ('excepCell');\n",
			"  var excepRow = document.getElementById ('excepRow');\n",
			"  if (numExcepFatal > 0) {\n",
			"    excepCell.firstChild.data = '' + numExcep + ' exceptions (' + numExcepFatal + ' fatal)';\n",
			"    if (excepRow.className == 'hover') excepRow.className = 'alert_hover';\n",
			"    if (excepRow.className == '') excepRow.className = 'alert';\n",
			"  } else if (numExcep > 0) {\n",
			"    excepCell.firstChild.data = '' + numExcep + ' exceptions';\n",
			"    if (excepRow.className == 'alert_hover') excepRow.className = 'hover';\n",
			"    if (excepRow.className == 'alert') excepRow.className = '';\n",
			"  }\n",
			"  showTableRow (excepRow, numExcep > 0 || numExcepFatal > 0);\n",
			"}\n",
			"</script>\n");
	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<tr",
			" id=\"excepRow\"",

			" onmouseover=\"%h\"",
			stringFormat (
				"%s; %s",
				"if (this.className=='alert') this.className='alert_hover'",
				"if (this.className=='') this.className='hover'"),

			" onmouseout=\"%h\"",
			stringFormat (
				"%s; %s",
				"if (this.className=='alert_hover') this.className='alert'",
				"if (this.className=='hover') this.className='';"),

			" onclick=\"%h\"",
			stringFormat (
				"top.frames['main'].location='%j'",
				requestContext.resolveApplicationUrl (
					"/exceptionLogs")),

			">\n",
			"<td id=\"excepCell\">-</td>\n",
			"</tr>\n");

	}

}
