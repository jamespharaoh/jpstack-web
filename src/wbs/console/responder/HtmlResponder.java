package wbs.console.responder;

import java.io.IOException;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;
import wbs.utils.time.TimeFormatter;

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
	Set <ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef> builder ()

			.addAll (
				myScriptRefs ())

			.build ();

	}

	protected
	Set <ScriptRef> myScriptRefs () {

		return ImmutableSet.of ();

	}

	protected
	Set <HtmlLink> htmlLinks () {

		return ImmutableSet.<HtmlLink> builder ()

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
	Set <HtmlLink> myHtmlLinks () {

		return ImmutableSet.of ();

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

		formatWriter.writeLineFormat (
			"<!DOCTYPE html>");

	}

	protected
	void renderHtmlStyleSheets () {

		formatWriter.writeLineFormat (
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

		formatWriter.writeLineFormat (
			"<title>%h</title>",
			getTitle ());

	}

	protected
	void renderHtmlScriptRefs () {

		for (
			ScriptRef scriptRef
				: scriptRefs ()
		) {

			formatWriter.writeLineFormat (
				"<script",
				" type=\"%h\"",
				scriptRef.getType (),
				" src=\"%h\"",
				scriptRef.getUrl (
					requestContext),
				"></script>");

		}

	}

	protected
	void renderHtmlLinks () {

		Set<? extends HtmlLink> links =
			htmlLinks ();

		if (links != null) {

			for (
				HtmlLink link
					: htmlLinks ()
			) {

				formatWriter.writeLineFormat (
					"%s",
					link.render (
						requestContext));

			}

		}

	}

	protected
	void renderHtmlHeadContents (
			@NonNull TaskLogger parentTaskLogger) {

		renderHtmlTitle ();

		renderHtmlScriptRefs ();

		renderHtmlLinks ();

		goMeta ();

	}

	protected
	void renderHtmlHead (
			@NonNull TaskLogger parentTaskLogger) {

		formatWriter.writeLineFormat (
			"<head>");

		formatWriter.increaseIndent ();

		renderHtmlHeadContents (
				parentTaskLogger);

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</head>");

	}

	protected
	void renderHtmlBodyContents (
			@NonNull TaskLogger taskLogger) {

	}

	protected
	void renderHtmlBody (
			@NonNull TaskLogger taskLogger) {

		formatWriter.writeLineFormat (
			"<body>");

		formatWriter.increaseIndent ();

		renderHtmlBodyContents (
			taskLogger);

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</body>");

	}

	@Override
	protected
	void render (
			@NonNull TaskLogger parentTaskLogger) {

		renderHtmlDoctype ();

		formatWriter.writeLineFormat (
			"<html>");

		renderHtmlHead (
			parentTaskLogger);

		renderHtmlBody (
			parentTaskLogger);

		formatWriter.writeLineFormat (
			"</html>");

	}

}
