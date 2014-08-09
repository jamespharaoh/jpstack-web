package wbs.platform.queue.console;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.joda.time.Instant;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.web.Responder;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.request.ConsoleRequestContext;

@PrototypeComponent ("queueFilterResponder")
public
class QueueFilterResponder
	implements Responder {

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	TimeFormatter timeFormatter;

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
			timeFormatter.instantToHttpTimestampString (
				Instant.now ()));

		InputStream inputStream =
			requestContext.getResourceAsStream (
				"/queue-filter.yml");

		OutputStream outputStream =
			requestContext.outputStream ();

		IOUtils.copy (
			inputStream,
			outputStream);

	}

}
