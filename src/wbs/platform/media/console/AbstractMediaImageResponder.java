package wbs.platform.media.console;

import static wbs.utils.string.StringUtils.stringEqualSafe;

import java.io.IOException;

import lombok.NonNull;

import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ConsoleResponder;

import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaObjectHelper;
import wbs.platform.media.model.MediaRec;

import wbs.utils.io.RuntimeIoException;

public abstract
class AbstractMediaImageResponder
	extends ConsoleResponder {

	// singleton dependencies

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	MediaObjectHelper mediaHelper;

	@SingletonDependency
	MediaLogic mediaLogic;

	// state

	MediaRec media;

	byte[] data;

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
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		media =
			mediaHelper.findRequired (
				requestContext.stuffIntegerRequired (
					"mediaId"));

		transform ();

	}

	protected
	void transform () {

		String rotate =
			requestContext.parameterOrEmptyString (
				"rotate");

		if (
			stringEqualSafe (
				rotate,
				"90")
		) {

			data =
				mediaLogic.writeImage (
					mediaLogic.rotateImage90 (
						mediaLogic.readImageRequired (
							getData (media),
							getMimeType (media))),
					getMimeType (media));

		} else if (
			stringEqualSafe (
				rotate,
				"180")
		) {

			data =
				mediaLogic.writeImage (
					mediaLogic.rotateImage180 (
						mediaLogic.readImageRequired (
							getData (media),
							getMimeType (media))),
					getMimeType (media));

		} else if (
			stringEqualSafe (
				rotate,
				"270")
		) {

			data =
				mediaLogic.writeImage (
					mediaLogic.rotateImage270 (
						mediaLogic.readImageRequired (
							getData (media),
							getMimeType (media))),
					getMimeType (media));

		} else {

			data =
				getData (media);

		}

	}

	@Override
	protected
	void setHtmlHeaders () {

		requestContext.setHeader (
			"Content-Type",
			getMimeType (media));

		requestContext.setHeader (
			"Content-Length",
			Integer.toString (
				data.length));

	}

	@Override
	protected
	void render (
			@NonNull TaskLogger parentTaskLogger) {

		try {

			requestContext.outputStream ().write (
				data);

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

}
