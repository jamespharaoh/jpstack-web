package wbs.platform.media.console;

import static wbs.utils.etc.Misc.runFilter;

import java.io.IOException;
import java.io.OutputStream;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ConsoleResponder;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

import wbs.platform.media.model.MediaRec;

import wbs.utils.io.RuntimeIoException;

@Log4j
@PrototypeComponent ("mediaVideoResponder")
public
class MediaVideoResponder
	extends ConsoleResponder {

	// singleton dependencies

	@SingletonDependency
	MediaConsoleHelper mediaHelper;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// state

	OutputStream out;
	byte[] data;

	@Override
	public
	void setup ()
		throws IOException {

		out =
			requestContext.outputStream ();

	}

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		MediaRec media =
			mediaHelper.findFromContextRequired ();

		try {

			data =
				runFilter (
					log,
					media.getContent ().getData (),
					".3gp",
					".flv",
					"ffmpeg",
					"-y",
					"-i",
					"<in>",
					"<out>");

		} catch (InterruptedException interruptedException) {

			Thread.currentThread ().interrupt ();

			throw new RuntimeException (
				interruptedException);

		}

	}

	@Override
	public
	void setHtmlHeaders () {

		requestContext.setHeader (
			"Content-Type",
			"video/x-flv");

		requestContext.setHeader (
			"Content-Length",
			Integer.toString (data.length));

	}

	@Override
	public
	void render (
			@NonNull TaskLogger parentTaskLogger) {

		try {

			out.write (
				data);

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

}
