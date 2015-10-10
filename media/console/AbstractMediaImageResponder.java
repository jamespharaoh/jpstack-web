package wbs.platform.media.console;

import java.io.IOException;

import javax.inject.Inject;

import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ConsoleResponder;
import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaObjectHelper;
import wbs.platform.media.model.MediaRec;

public abstract
class AbstractMediaImageResponder
	extends ConsoleResponder {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	MediaObjectHelper mediaHelper;

	@Inject
	MediaLogic mediaLogic;

	// state

	byte[] data;
	String mimeType;

	// hooks

	protected abstract
	byte[] getData (
			MediaRec media);

	protected abstract
	String getMimeType (
			MediaRec media);

	// implementation

	@Override
	protected
	void prepare () {

		int mediaId =
			requestContext.stuffInt (
				"mediaId");

		MediaRec media =
			mediaHelper.find (
				mediaId);

		data =
			getData (
				media);

		mimeType =
			getMimeType (
				media);

		transform ();

	}

	protected
	void transform () {

		String rotate =
			requestContext.parameter (
				"rotate");

		if ("90".equals (rotate)) {

			data =
				mediaLogic.writeImage (
					mediaLogic.rotateImage90 (
						mediaLogic.readImage (
							data,
							mimeType)),
					mimeType);

		} else if ("180".equals (rotate)) {

			data =
				mediaLogic.writeImage (
					mediaLogic.rotateImage180 (
						mediaLogic.readImage (
							data,
							mimeType)),
					mimeType);

		} else if ("270".equals (rotate)) {

			data =
				mediaLogic.writeImage (
					mediaLogic.rotateImage270 (
						mediaLogic.readImage (
							data,
							mimeType)),
					mimeType);

		}

	}

	@Override
	protected
	void setHtmlHeaders () {

		requestContext.setHeader (
			"Content-Type",
			mimeType);

		requestContext.setHeader (
			"Content-Length",
			Integer.toString (
				data.length));

	}

	@Override
	protected void render ()
		throws IOException {

		requestContext.outputStream ().write (
			data);

	}

}
