package wbs.platform.media.console;

import static wbs.framework.utils.etc.Misc.runFilter;

import java.io.IOException;
import java.io.OutputStream;

import javax.inject.Inject;

import lombok.extern.log4j.Log4j;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ConsoleResponder;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.media.model.MediaRec;

@Log4j
@PrototypeComponent ("mediaVideoResponder")
public
class MediaVideoResponder
	extends ConsoleResponder {

	@Inject
	MediaConsoleHelper mediaHelper;

	@Inject
	ConsoleRequestContext requestContext;

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
	void prepare () {

		MediaRec media =
			mediaHelper.findRequired (
				requestContext.stuffInteger (
					"mediaId"));

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
	void render ()
		throws IOException {

		out.write (
			data);

	}

}
