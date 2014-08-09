package wbs.platform.console.responder;

import java.io.IOException;
import java.util.Set;

import javax.inject.Inject;

import org.joda.time.Instant;

import wbs.platform.console.html.HtmlLink;
import wbs.platform.console.html.ScriptRef;
import wbs.platform.console.misc.TimeFormatter;

import com.google.common.collect.ImmutableSet;

public abstract
class HtmlResponder
	extends ConsolePrintResponder {

	@Inject
	TimeFormatter timeFormatter;

	protected
	String getTitle () {
		return "Untitled";
	}

	protected
	Set<ScriptRef> scriptRefs () {
		return ImmutableSet.<ScriptRef>of ();
	}

	protected
	Set<HtmlLink> getLinks () {

		return ImmutableSet.<HtmlLink>of (

			HtmlLink.cssStyle (
				requestContext.resolveApplicationUrl (
					"/style/basic.css")),

			HtmlLink.icon (
				requestContext.resolveApplicationUrl (
					"/favicon.ico")),

			HtmlLink.shortcutIcon (
				requestContext.resolveApplicationUrl (
					"/favicon.ico")));

	}

	@Override
	protected
	void goHeaders ()
		throws IOException {

		super.goHeaders ();

		requestContext.setHeader (
			"Content-Type",
			"text/html; charset=utf-8");

		requestContext.setHeader (
			"Cache-Control",
			"no-cache");

		requestContext.setHeader (
			"Expiry",
			timeFormatter.instantToHttpTimestampString (
				Instant.now ()));

	}

	protected
	void goDoctype () {

		printFormat (
			"<!DOCTYPE html>\n");

	}

	protected
	void goStyleSheets () {

		printFormat (
			"<link",
			" rel=\"stylesheet\"",
			" href=\"%h\"",
			requestContext.resolveApplicationUrl (
				"/style/basic.css"),
			">");

	}

	protected
	void goMetaRefresh () {
	}

	protected
	void goMeta () {
		goMetaRefresh ();
	}

	protected
	void goTitle () {

		printFormat (
			"<title>%h</title>\n",
			getTitle ());

	}

	protected
	void goScriptRefs () {

		for (ScriptRef scriptRef
				: scriptRefs ()) {

			printFormat (
				"<script",
				" type=\"%h\"",
				scriptRef.getType (),
				" src=\"%h\"",
				scriptRef.getUrl (
					requestContext),
				"></script>\n");

		}

	}

	protected
	void goLinks () {

		Set<? extends HtmlLink> links =
			getLinks ();

		if (links != null) {

			for (HtmlLink link
					: getLinks ()) {

				printFormat (
					"%s\n",
					link.toString ());

			}

		}

	}

	protected
	void goHeadStuff () {

		goTitle ();
		goScriptRefs ();
		goLinks ();
		goMeta ();

	}

	protected
	void goHead () {

		printFormat (
			"<head>\n");

		goHeadStuff ();

		printFormat (
			"</head>\n");

	}

	protected
	void goBodyStuff () {
	}

	protected
	void goBody () {

		printFormat (
			"<body>\n");

		goBodyStuff ();

		printFormat (
			"</body>\n");

	}

	@Override
	protected
	void goContent () {

		goDoctype ();

		printFormat (
			"<html>\n");

		goHead ();
		goBody ();

		printFormat (
			"</html>\n");

	}

}
