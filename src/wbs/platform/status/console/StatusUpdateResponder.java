package wbs.platform.status.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.inject.Inject;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.joda.time.Instant;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.console.responder.ConsoleResponder;

@PrototypeComponent ("statusUpdateResponder")
public
class StatusUpdateResponder
	extends ConsoleResponder {

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	StatusLineManager statusLineManager;

	@Inject
	TimeFormatter timeFormatter;

	String javascript;

	@Override
	protected
	void prepare () {

		if (requestContext.userId () == null) {

			javascript =
				stringFormat (
					"window.top.location = '%h';",
					requestContext.resolveApplicationUrl (
						"/"));

			return;

		}

		// create the html

		StringWriter stringWriter =
			new StringWriter ();

		PrintWriter printWriter =
			new PrintWriter (stringWriter);

		printWriter.print (
			stringFormat (
				"updateTimestamp ('%j');\n",
				timeFormatter.instantToTimestampString (
					timeFormatter.defaultTimezone (),
					Instant.now ())));

		for (StatusLine statusLine
				: statusLineManager.getStatusLines ()) {

			printWriter.print (
				statusLine.getUpdateScript ());

		}

		javascript =
			stringWriter.toString ();

	}

	@Override
	protected
	void setHtmlHeaders ()
		throws IOException {

		super.setHtmlHeaders ();

		requestContext.setHeader (
			"Content-Type",
			"text/xml");

		requestContext.setHeader (
			"Cache-Control",
			"no-cache");

		requestContext.setHeader (
			"Expiry",
			timeFormatter.instantToHttpTimestampString (
				Instant.now ()));

	}

	@Override
	protected
	void render ()
		throws IOException {

		Element statusUpdateElem =
			new Element ("status-update");

		Document document =
			new Document (statusUpdateElem);

		Element javascriptElem =
			new Element ("javascript");

		statusUpdateElem.addContent (
			javascriptElem);

		javascriptElem.setText (
			javascript);

		XMLOutputter xmlOutputter =
			new XMLOutputter (
				Format.getPrettyFormat ());

		xmlOutputter.output (
			document,
			requestContext.writer ());

	}

}
