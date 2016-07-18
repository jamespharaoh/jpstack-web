package wbs.platform.queue.console;

import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.joinWithSeparator;

import java.io.IOException;

import javax.inject.Inject;

import lombok.Cleanup;

import org.joda.time.Instant;

import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.utils.TimeFormatter;
import wbs.framework.web.Responder;
import wbs.platform.user.console.UserConsoleLogic;

@PrototypeComponent ("queueFilterResponder")
public
class QueueFilterResponder
	implements Responder {

	// dependencies

	@Inject
	Database database;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	TimeFormatter timeFormatter;

	@Inject
	UserConsoleLogic userConsoleLogic;

	// implementation

	@Override
	public
	void execute ()
		throws IOException {

		requestContext.setHeader (
			"Content-Type",
			"text/plain");

		requestContext.setHeader (
			"Cache-Control",
			"no-cache");

		requestContext.setHeader (
			"Expiry",
			timeFormatter.httpTimestampString (
				Instant.now ()));

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"QueueFilterResponder.execute ()",
				this);

		String filter =
			ifNull (
				userConsoleLogic.sliceRequired ().getFilter (),
				defaultFilter);

		requestContext.outputStream ().write (
			filter.getBytes ());

	}

	final static
	String defaultFilter =
		joinWithSeparator (
			"\n",
			"---",
			"- name: No Filter",
			"  options:",
			"    - name: All");

}
