package wbs.platform.core.console;

import static wbs.web.utils.HtmlScriptUtils.htmlScriptBlockClose;
import static wbs.web.utils.HtmlScriptUtils.htmlScriptBlockOpen;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ConsolePrintResponder;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.time.TimeFormatter;

@PrototypeComponent ("coreFrameSetResponder")
public
class CoreFrameSetResponder
	extends ConsolePrintResponder {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	TimeFormatter timeFormatter;

	@SingletonDependency
	WbsConfig wbsConfig;

	// implementation

	@Override
	public
	void render (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"render");

		) {

			goDocType (
				transaction);

			goHtml (
				transaction);

		}

	}

	@Override
	public
	void setHtmlHeaders (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"setHtmlHeaders");

		) {

			super.setHtmlHeaders (
				transaction);

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

	}

	public
	void goDocType (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goDocType");

		) {

			formatWriter.writeLineFormat (
				"<!DOCTYPE html>");

		}

	}

	public
	void goHtml (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goHtml");

		) {

			formatWriter.writeLineFormat (
				"<html>");

			goHtmlStuff (
				transaction);

			formatWriter.writeLineFormat (
				"</html>");

		}

	}

	public
	void goHtmlStuff (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goHtmlStuff");

		) {

			goHead (
				transaction);

			goFrameset (
				transaction);

		}

	}

	public
	void goHead (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goHead");

		) {

			formatWriter.writeLineFormatIncreaseIndent (
				"<head>");

			goHeadStuff (
				transaction);

			formatWriter.writeLineFormatDecreaseIndent (
				"</head>");

		}

	}

	public
	void goHeadStuff (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goHeadStuff");

		) {

			goTitle (
				transaction);

			goScripts (
				transaction);

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

	}

	public
	void goTitle (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goTitle");

		) {

			formatWriter.writeLineFormatIncreaseIndent (
				"<title>");

			formatWriter.writeLineFormat (
				"%h",
				wbsConfig.consoleTitle ());

			formatWriter.writeLineFormatDecreaseIndent (
				"</title>");

		}

	}

	public
	void goScripts (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goScripts");

		) {

			htmlScriptBlockOpen ();

			formatWriter.writeLineFormatIncreaseIndent (
				"function show_inbox (show) {");

			formatWriter.writeLineFormat (
				"document.getElementById ('right_frameset').rows =");

			formatWriter.writeLineFormat (
				"  show ? '2*,1*' : '*,0';");

			formatWriter.writeLineFormatDecreaseIndent (
				"}");

			htmlScriptBlockClose ();

		}

	}

	public
	void goFrameset (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"goFrameset");

		) {

			formatWriter.writeLineFormatIncreaseIndent (
				"<frameset cols=\"1*,4*\">");

			formatWriter.writeLineFormatIncreaseIndent (
				"<frameset rows=\"2*,1*\">");

			formatWriter.writeLineFormat (
				"<frame name=\"sidebar\" src=\"%h\">",
				requestContext.resolveApplicationUrl (
					"/sidebar"));

			formatWriter.writeLineFormat (
				"<frame name=\"status\" src=\"%h\">",
				requestContext.resolveApplicationUrl (
					"/status"));

			formatWriter.writeLineFormatDecreaseIndent (
				"</frameset>");

			formatWriter.writeLineFormat (
				"<frameset rows=\"1*,0\" id=\"right_frameset\">");

			formatWriter.writeLineFormatIncreaseIndent (
				"<frame name=\"main\" src=\"%h\">",
				requestContext.resolveApplicationUrl (
					"/home"));

			formatWriter.writeLineFormat (
				"<frame name=\"inbox\" src=\"%h\">",
				requestContext.resolveApplicationUrl (
					"/queues/queue.home"));

			formatWriter.writeLineFormatDecreaseIndent (
				"</frameset>");

			formatWriter.writeLineFormatDecreaseIndent (
				"</frameset>");

		}

	}

}
