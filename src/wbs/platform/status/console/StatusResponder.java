package wbs.platform.status.console;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.part.PagePart;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.console.responder.HtmlResponder;

@PrototypeComponent ("statusResponder")
public
class StatusResponder
	extends HtmlResponder {

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	StatusLineManager statusLineManager;

	List<PagePart> pageParts =
		new ArrayList<PagePart> ();

	@Override
	protected
	void setup ()
			throws IOException {

		super.setup ();

		for (StatusLine statusLine
				: statusLineManager.getStatusLines ()) {

			PagePart pagePart =
				statusLine.get ();

			pagePart.setup (
				Collections.<String,Object>emptyMap ());

			pageParts.add (pagePart);

		}

	}

	@Override
	protected
	void prepare () {

		super.prepare ();

		for (PagePart pagePart : pageParts)
			pagePart.prepare ();

	}

	@Override
	protected
	void goHeadStuff () {

		super.goHeadStuff ();

		printFormat (
			"<style type=\"text/css\">\n",
			"#timeRow { display: none; }\n",
			"</style>\n");

		printFormat (
			"<script type=\"text/javascript\">\n");

		// config

		printFormat (
			"var statusRequestUrl = '%j';\n",
			requestContext.resolveApplicationUrl (
				"/status.update"));

		printFormat (
			"var statusRequestTime = 1000;\n");

		// status

		printFormat (
			"var statusRequest;\n");

		// sets up the request

		printFormat (
			"function statusRequestGo () {\n",

			"  if (window.XMLHttpRequest) {\n",
			"    statusRequest = new XMLHttpRequest ();\n",
			"  } else if (window.ActiveXObject) {\n",
			"    statusRequest = new ActiveXObject (\"Microsoft.XMLHTTP\");\n",
			"  } else return;\n",

			"  statusRequest.onreadystatechange = statusRequestChange;\n",
			"  statusRequest.open (\"GET\", statusRequestUrl, true);\n",
			"  statusRequest.send (null);\n",
			"}\n");

		// handles the request status change events

		printFormat (
			"function statusRequestChange () {\n",

			"  if (statusRequest.readyState != 4) return;\n",

			"  if (statusRequest.status != 200) {\n",
			"    document.getElementById ('headerCell').firstChild.data =\n",
			"     'Status (' + statusRequest.status + '!)';\n",
			"    statusRequestSchedule ();\n",
			"    return;\n",
			"  }\n",

			"  try {\n",

			"    var statusDiv = document.getElementById ('statusDiv');\n",
			"    var response = statusRequest.responseXML.documentElement;\n",
			"    eval (response.getElementsByTagName ('javascript') [0].firstChild.data);\n",

			"    document.getElementById ('headerCell').firstChild.data = 'Status';\n",

			"    var loadingRow = document.getElementById ('loadingRow');\n",
			"    showTableRow (loadingRow, false);\n",

			"  } catch (e) { }\n",

			"  statusRequestSchedule ();\n",
			"}\n");

		// sets a timer for the request

		printFormat (
			"function statusRequestSchedule () {\n",
			"  setTimeout (\"statusRequestGo ()\", statusRequestTime);\n",
			"}\n");

		// shows or hides the given table row

		printFormat (
			"function showTableRow (row, show) {\n",
			"  if (show && row.style.display != 'table-row' && row.style.display != 'block') {\n",
			"    try { row.style.display = 'table-row'; }\n",
			"    catch (e) { row.style.display = 'block'; }\n",
			"  }\n",
			"  if (! show && row.style.display != 'none') {\n",
			"    row.style.display = 'none';\n",
			"  }\n",
			"}\n");

		printFormat (
			"function updateTimestamp (timestamp) {\n",
			"  var timeCell = document.getElementById ('timeCell');\n",
			"  var timeRow = document.getElementById ('timeRow');\n",
			"  timeCell.firstChild.data = timestamp;\n",
			"  showTableRow (timeRow, true);\n",
			"}\n");

		printFormat (
			"</script>\n");

		for (PagePart pagePart
				: pageParts)
			pagePart.goHeadStuff ();

	}

	@Override
	protected
	void goBody () {

		printFormat (
			"<body onload=\"statusRequestSchedule ();\">");

		goBodyStuff ();

		printFormat (
			"</body>");

	}

	@Override
	protected
	void goBodyStuff () {

		printFormat (
			"<table",
			" id=\"statusTable\"",
			" class=\"list\"",
			" width=\"100%%\"",
			">\n");

		printFormat (
			"<tr>\n",
			"<th id=\"headerCell\">Status</th>\n",
			"</tr>\n");

		printFormat (
			"<tr id=\"loadingRow\">\n",
			"<td id=\"loadingCell\">Loading...</td>\n",
			"</tr>\n");

		printFormat (
			"<tr id=\"timeRow\">\n",
			"<td id=\"timeCell\">-</td>\n",
			"</tr>\n");

		for (PagePart pagePart : pageParts)
			pagePart.goBodyStuff();

		printFormat (
			"<tr>\n",

			"<td><form\n",
			" action=\"logoff\"",
			" method=\"post\"",
			">",

			"<input",
			" type=\"submit\"",
			" value=\"log out\"",
			">",

			"</form></td>\n",

			"</tr>\n");

		printFormat (
			"</table>\n");

	}

}
