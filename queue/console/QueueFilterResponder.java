package wbs.platform.queue.console;

import static wbs.utils.etc.IoUtils.writeBytes;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.joinWithNewline;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.user.console.UserConsoleLogic;

import wbs.utils.time.TimeFormatter;

import wbs.web.responder.Responder;

@PrototypeComponent ("queueFilterResponder")
public
class QueueFilterResponder
	implements Responder {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	TimeFormatter timeFormatter;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// implementation

	@Override
	public
	void execute (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"execute");

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

		try (

			Transaction transaction =
				database.beginReadOnly (
					taskLogger,
					"QueueFilterResponder.execute ()",
					this);

		) {

			String filter =
				ifNull (
					userConsoleLogic.sliceRequired ().getFilter (),
					defaultFilter);

			writeBytes (
				requestContext.outputStream (),
				filter.getBytes ());

		}

	}

	final static
	String defaultFilter =
		joinWithNewline (
			"---",
			"- name: No Filter",
			"  options:",
			"    - name: All");

}
