package wbs.smsapps.manualresponder.console;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.part.AbstractPagePart;

@PrototypeComponent ("manualResponderStatusLinePart")
public
class ManualResponderStatusLinePart
	extends AbstractPagePart {

	@Override
	public
	void goHeadStuff () {

		printFormat (

			"<style type=\"text/css\">\n",

			"  #manualResponderRow { display: none; }\n",

			"</style>\n");

		printFormat (

			"<script type=\"text/javascript\">\n",

			"  function updateManualResponder (numToday, numThisHour) {\n",

			"    var cell = $('#manualResponderCell');\n",
			"    var row = $('#manualResponderRow');\n",

			"    cell.text ('Messages answered: ' + [\n",
			"      String (numToday) + ' today',\n",
			"      String (numThisHour) + ' this hour',\n",
			"    ].join (', '));\n",

			"    showTableRow (row [0], numToday > 0 || numThisHour > 0);\n",

			"  }\n",

			"</script>\n");

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<tr",
			" id=\"manualResponderRow\"",
			">\n",
			"<td id=\"manualResponderCell\">-</td>\n",
			"</tr>\n");

	}

}
