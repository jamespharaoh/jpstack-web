package wbs.platform.status.console;

import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.inject.Inject;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.joda.time.Instant;

import wbs.console.misc.TimeFormatter;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ConsoleResponder;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.scaffold.console.RootConsoleHelper;
import wbs.platform.scaffold.model.RootRec;

@PrototypeComponent ("statusUpdateResponder")
public
class StatusUpdateResponder
	extends ConsoleResponder {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	RootConsoleHelper rootHelper;

	@Inject
	StatusLineManager statusLineManager;

	@Inject
	TimeFormatter timeFormatter;

	// state

	RootRec root;
	String javascript;

	// implementation

	@Override
	protected
	void prepare () {

		root =
			rootHelper.find (0);

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
				timeFormatter.instantToTimestampTimezoneString (
					timeFormatter.defaultTimezone (),
					Instant.now ())));

		if (
			isNotNull (
				root.getNotice ())
		) {

			printWriter.print (
				stringFormat (
					"updateNotice ('%j');\n",
					root.getNotice ()));

		} else {

			printWriter.print (
				stringFormat (
					"updateNotice (undefined);\n"));

		}

		for (
			StatusLine statusLine
				: statusLineManager.getStatusLines ()
		) {

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
