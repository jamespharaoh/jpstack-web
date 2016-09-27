package wbs.platform.core.console;

import static wbs.utils.web.HtmlScriptUtils.htmlScriptBlockClose;
import static wbs.utils.web.HtmlScriptUtils.htmlScriptBlockOpen;

import java.io.IOException;

import org.joda.time.Instant;

import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ConsolePrintResponder;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.utils.time.TimeFormatter;

@PrototypeComponent ("coreFrameSetResponder")
public
class CoreFrameSetResponder
	extends ConsolePrintResponder {

	// singleton dependencies

	@SingletonDependency
	WbsConfig wbsConfig;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	TimeFormatter timeFormatter;

	// implementation

	@Override
	public
	void render () {

		goDocType ();

		goHtml ();

	}

	@Override
	public
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

	public
	void goDocType () {

		formatWriter.writeLineFormat (
			"<!DOCTYPE html>");

	}

	public
	void goHtml () {

		formatWriter.writeLineFormat (
			"<html>");

		goHtmlStuff ();

		formatWriter.writeLineFormat (
			"</html>");

	}

	public
	void goHtmlStuff () {

		goHead ();

		goFrameset ();

	}

	public
	void goHead () {

		formatWriter.writeLineFormat (
			"<head>");

		formatWriter.increaseIndent ();

		goHeadStuff ();

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</head>");

	}

	public
	void goHeadStuff () {

		goTitle ();

		goScripts ();

		formatWriter.writeLineFormat (
			"<link",
			" rel=\"shortcut icon\"",
			" href=\"%h\"",
			requestContext.resolveApplicationUrl (
				"/favicon.ico"),
			">");

		formatWriter.writeLineFormat (
			"<link",
			" rel=\"icon\"",
			" href=\"%h\"",
			requestContext.resolveApplicationUrl (
				"/favicon.ico"),
			">");

	}

	public
	void goTitle () {

		formatWriter.writeLineFormat (
			"<title>");

		formatWriter.increaseIndent ();

		formatWriter.writeLineFormat (
			"%h",
			wbsConfig.consoleTitle ());

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</title>");

	}

	public
	void goScripts () {

		htmlScriptBlockOpen ();

		formatWriter.writeLineFormat (
			"function show_inbox (show) {");

		formatWriter.increaseIndent ();

		formatWriter.writeLineFormat (
			"document.getElementById ('right_frameset').rows =");

		formatWriter.writeLineFormat (
			"  show ? '2*,1*' : '*,0';");

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"}");

		htmlScriptBlockClose ();

	}

	public
	void goFrameset () {

		formatWriter.writeLineFormat (
			"<frameset cols=\"1*,4*\">");

		formatWriter.increaseIndent ();

		formatWriter.writeLineFormat (
			"<frameset rows=\"2*,1*\">");

		formatWriter.increaseIndent ();

		formatWriter.writeLineFormat (
			"<frame name=\"sidebar\" src=\"%h\">",
			requestContext.resolveApplicationUrl (
				"/sidebar"));

		formatWriter.writeLineFormat (
			"<frame name=\"status\" src=\"%h\">",
			requestContext.resolveApplicationUrl (
				"/status"));

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</frameset>");

		formatWriter.writeLineFormat (
			"<frameset rows=\"1*,0\" id=\"right_frameset\">");

		formatWriter.increaseIndent ();

		formatWriter.writeLineFormat (
			"<frame name=\"main\" src=\"%h\">",
			requestContext.resolveApplicationUrl (
				"/home"));

		formatWriter.writeLineFormat (
			"<frame name=\"inbox\" src=\"%h\">",
			requestContext.resolveApplicationUrl (
				"/queues/queue.home"));

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</frameset>");

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</frameset>");

	}

}
