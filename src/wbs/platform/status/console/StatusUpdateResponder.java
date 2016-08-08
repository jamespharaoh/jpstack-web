package wbs.platform.status.console;

import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.inject.Inject;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.joda.time.Instant;

import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ConsoleResponder;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.config.WbsConfig;
import wbs.platform.scaffold.console.RootConsoleHelper;
import wbs.platform.scaffold.model.RootRec;
import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

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
	UserConsoleLogic userConsoleLogic;

	@Inject
	UserObjectHelper userHelper;

	@Inject
	WbsConfig wbsConfig;

	// state

	RootRec root;
	UserRec user;
	SliceRec slice;

	String javascript;

	// implementation

	@Override
	protected
	void prepare () {

		// redirect to login page if not logged in

		if (userConsoleLogic.notLoggedIn ()) {

			javascript =
				stringFormat (
					"window.top.location = '%h';",
					requestContext.resolveApplicationUrl (
						"/"));

			return;

		}

		// find objects

		root =
			rootHelper.findRequired (
				0);

		// create the html

		StringWriter stringWriter =
			new StringWriter ();

		PrintWriter printWriter =
			new PrintWriter (stringWriter);

		printWriter.print (
			stringFormat (
				"updateTimestamp ('%j');\n",
				userConsoleLogic.timestampWithTimezoneString (
					transaction.now ())));

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
			userConsoleLogic.httpTimestampString (
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
