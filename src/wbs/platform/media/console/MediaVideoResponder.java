package wbs.platform.media.console;

import static wbs.framework.utils.etc.Misc.runFilter;

import java.io.IOException;
import java.io.OutputStream;

import javax.inject.Inject;

import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.console.responder.ConsoleResponder;
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

		int mediaId =
			requestContext.stuffInt ("mediaId");

		MediaRec media =
			mediaHelper.find (mediaId);

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

	}

	@Override
	public
	void goHeaders () {

		requestContext.setHeader (
			"Content-Type",
			"video/x-flv");

		requestContext.setHeader (
			"Content-Length",
			Integer.toString (data.length));

	}

	@Override
	public
	void goContent ()
		throws IOException {

		out.write (
			data);

	}

}
