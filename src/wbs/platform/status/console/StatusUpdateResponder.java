package wbs.platform.status.console;

import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.presentInstances;
import static wbs.utils.string.StringUtils.joinWithSeparator;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.thread.ConcurrentUtils.futureGet;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;

import lombok.NonNull;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.joda.time.Instant;

import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ConsoleResponder;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.deployment.logic.DeploymentLogic;
import wbs.platform.deployment.model.ConsoleDeploymentRec;
import wbs.platform.scaffold.console.RootConsoleHelper;
import wbs.platform.scaffold.model.RootRec;
import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

import wbs.utils.io.RuntimeIoException;
import wbs.utils.string.StringFormatWriter;

@PrototypeComponent ("statusUpdateResponder")
public
class StatusUpdateResponder
	extends ConsoleResponder {

	// singleton dependencies

	@SingletonDependency
	DeploymentLogic deploymentLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	RootConsoleHelper rootHelper;

	@SingletonDependency
	StatusLineManager statusLineManager;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserObjectHelper userHelper;

	@SingletonDependency
	WbsConfig wbsConfig;

	// state

	RootRec root;
	UserRec user;
	SliceRec slice;

	String javascript;

	// implementation

	@Override
	protected
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"prepare");

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
				0l);

		ConsoleDeploymentRec consoleDeployment =
			deploymentLogic.thisConsoleDeployment ();

		// get status lines

		List <Future <String>> futures =
			iterableMapToList (
				statusLine ->
					statusLine.getUpdateScript (
						taskLogger),
				statusLineManager.getStatusLines ());

		// create the html

		try (

			StringFormatWriter formatWriter =
				new StringFormatWriter ();

		) {

			formatWriter.writeFormat (
				"updateHeader ('%j');\n",
				joinWithSeparator (
					" – ",
					presentInstances (
						optionalOf (
							"Status"),
						optionalFromNullable (
							consoleDeployment.getStatusLabel ()),
						optionalOf (
							deploymentLogic.gitVersion ()))));
	
			formatWriter.writeFormat (
				"updateTimestamp ('%j');\n",
				userConsoleLogic.timestampWithTimezoneString (
					transaction.now ()));
	
			if (
				isNotNull (
					root.getNotice ())
			) {
	
				formatWriter.writeFormat (
					"updateNotice ('%j');\n",
					root.getNotice ());
	
			} else {
	
				formatWriter.writeFormat (
					"updateNotice (undefined);\n");
	
			}
	
			// close transaction
	
			transaction.close ();
	
			// wait for status lines
	
			futures.forEach (
				future ->
					formatWriter.writeString (
					futureGet (
						future)));
	
			// convert to string
	
			javascript =
				formatWriter.toString ();

		}

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
	void render (
			@NonNull TaskLogger parentTaskLogger) {

		Element statusUpdateElem =
			new Element (
				"status-update");

		Document document =
			new Document (
				statusUpdateElem);

		Element javascriptElem =
			new Element (
				"javascript");

		statusUpdateElem.addContent (
			javascriptElem);

		javascriptElem.setText (
			javascript);

		XMLOutputter xmlOutputter =
			new XMLOutputter (
				Format.getPrettyFormat ());

		try {

			xmlOutputter.output (
				document,
				requestContext.printWriter ());

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

}
