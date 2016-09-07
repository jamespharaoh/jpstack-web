package wbs.console.responder;

import java.io.IOException;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import org.joda.time.Instant;

import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.utils.TimeFormatter;

public abstract
class HtmlResponder
	extends ConsolePrintResponder {

	// singleton dependencies

	@SingletonDependency
	TimeFormatter timeFormatter;

	// details

	protected
	String getTitle () {
		return "Untitled";
	}

	protected
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				myScriptRefs ())

			.build ();

	}

	protected
	Set<ScriptRef> myScriptRefs () {

		return ImmutableSet.<ScriptRef>of ();

	}

	protected
	Set<HtmlLink> htmlLinks () {

		return ImmutableSet.<HtmlLink>builder ()

			.add (
				HtmlLink.applicationCssStyle (
					"/style/basic.css"))

			.add (
				HtmlLink.applicationIcon (
					"/favicon.ico"))

			.add (
				HtmlLink.applicationShortcutIcon (
					"/favicon.ico"))

			.addAll (
				myHtmlLinks ())

			.build ();

	}

	protected
	Set<HtmlLink> myHtmlLinks () {

		return ImmutableSet.<HtmlLink>of ();

	}

	@Override
	protected
	void setHtmlHeaders ()
		throws IOException {

		super.setHtmlHeaders ();

		requestContext.setHeader (
			"Content-Type",
			"text/html; charset=utf-8");

		requestContext.setHeader (
			"Cache-Control",
			"no-cache");

		requestContext.setHeader (
			"Expiry",
			timeFormatter.httpTimestampString (
				Instant.now ()));

	}

	protected
	void renderHtmlDoctype () {

		printFormat (
			"<!DOCTYPE html>\n");

	}

	protected
	void renderHtmlStyleSheets () {

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
	void renderHtmlTitle () {

		printFormat (
			"<title>%h</title>\n",
			getTitle ());

	}

	protected
	void renderHtmlScriptRefs () {

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
	void renderHtmlLinks () {

		Set<? extends HtmlLink> links =
			htmlLinks ();

		if (links != null) {

			for (HtmlLink link
					: htmlLinks ()) {

				printFormat (
					"%s\n",
					link.render (
						requestContext));

			}

		}

	}

	protected
	void renderHtmlHeadContents () {

		renderHtmlTitle ();
		renderHtmlScriptRefs ();
		renderHtmlLinks ();
		goMeta ();

	}

	protected
	void renderHtmlHead () {

		printFormat (
			"<head>\n");

		renderHtmlHeadContents ();

		printFormat (
			"</head>\n");

	}

	protected
	void renderHtmlBodyContents () {
	}

	protected
	void renderHtmlBody () {

		printFormat (
			"<body>\n");

		renderHtmlBodyContents ();

		printFormat (
			"</body>\n");

	}

	@Override
	protected
	void render () {

		renderHtmlDoctype ();

		printFormat (
			"<html>\n");

		renderHtmlHead ();
		renderHtmlBody ();

		printFormat (
			"</html>\n");

	}

}
