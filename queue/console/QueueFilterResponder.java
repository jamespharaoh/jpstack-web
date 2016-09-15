package wbs.platform.queue.console;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.joinWithNewline;

import java.io.IOException;

import lombok.Cleanup;

import org.joda.time.Instant;

import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.utils.time.TimeFormatter;

@PrototypeComponent ("queueFilterResponder")
public
class QueueFilterResponder
	implements Responder {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	TimeFormatter timeFormatter;

	@SingletonDependency
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
		joinWithNewline (
			"---",
			"- name: No Filter",
			"  options:",
			"    - name: All");

}
