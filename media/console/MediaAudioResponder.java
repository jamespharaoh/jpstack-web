package wbs.platform.media.console;

import java.io.IOException;
import java.io.OutputStream;

import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ConsoleResponder;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.media.model.MediaObjectHelper;
import wbs.platform.media.model.MediaRec;

@PrototypeComponent ("mediaAudioResponder")
public
class MediaAudioResponder
	extends ConsoleResponder {

	// singleton dependencies

	@SingletonDependency
	MediaObjectHelper mediaHelper;

	@SingletonDependency
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

		data =
			media.getContent ().getData ();

	}

	@Override
	public
	void setHtmlHeaders () {

		requestContext.setHeader (
			"Content-Type",
			"audio/mpeg");

		requestContext.setHeader (
			"Content-Length",
			Integer.toString (data.length));

	}

	@Override
	public
	void render ()
		throws IOException {

		out.write (data);

	}

}
