package wbs.platform.media.console;

import java.io.IOException;
import java.io.OutputStream;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.console.responder.ConsoleResponder;
import wbs.platform.media.model.MediaObjectHelper;
import wbs.platform.media.model.MediaRec;

@PrototypeComponent ("mediaAudioResponder")
public
class MediaAudioResponder
	extends ConsoleResponder {

	@Inject
	MediaObjectHelper mediaHelper;

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
			media.getContent ().getData ();

	}

	@Override
	public
	void goHeaders () {

		requestContext.setHeader (
			"Content-Type",
			"audio/mpeg");

		requestContext.setHeader (
			"Content-Length",
			Integer.toString (data.length));

	}

	@Override
	public
	void goContent ()
		throws IOException {

		out.write (data);

	}

}
