package wbs.platform.status.console;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.google.common.collect.ImmutableSet;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.part.PagePart;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.HtmlResponder;
import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("statusResponder")
public
class StatusResponder
	extends HtmlResponder {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	StatusLineManager statusLineManager;

	// state

	List<PagePart> pageParts =
		new ArrayList<PagePart> ();

	// details

	@Override
	protected
	Set<ScriptRef> myScriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

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
					Set<ScriptRef>::stream)

				.iterator ())

			.build ();

	}

	// implementation

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
	void renderHtmlHeadContents () {

		super.renderHtmlHeadContents ();

		printFormat (
			"<style type=\"text/css\">\n",
			"#timeRow { display: none; }\n",
			"#noticeRow { display: none; }\n",
			"</style>\n");

		// config

		printFormat (
			"<script type=\"text/javascript\">\n");

		printFormat (
			"var statusRequestUrl = '%j';\n",
			requestContext.resolveApplicationUrl (
				"/status.update"));

		printFormat (
			"var statusRequestTime = 800;\n");

		printFormat (
			"</script>\n");

		// page parts

		for (
			PagePart pagePart
				: pageParts
		) {
			pagePart.renderHtmlHeadContent ();
		}

	}

	@Override
	protected
	void renderHtmlBody () {

		printFormat (
			"<body onload=\"statusRequestSchedule ();\">");

		renderHtmlBodyContents ();

		printFormat (
			"</body>");

	}

	@Override
	protected
	void renderHtmlBodyContents () {

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
			"<tr id=\"noticeRow\">\n",
			"<td id=\"noticeCell\">&mdash;</td>\n",
			"</tr>\n");

		printFormat (
			"<tr id=\"timeRow\">\n",
			"<td id=\"timeCell\">&mdash;</td>\n",
			"</tr>\n");

		for (PagePart pagePart : pageParts)
			pagePart.renderHtmlBodyContent();

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
