package wbs.platform.media.console;

import static wbs.utils.string.StringUtils.stringEqualSafe;

import java.io.IOException;

import lombok.NonNull;

import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ConsoleResponder;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.model.MediaObjectHelper;
import wbs.platform.media.model.MediaRec;

import wbs.utils.io.RuntimeIoException;

public abstract
class AbstractMediaImageResponder
	extends ConsoleResponder {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaObjectHelper mediaHelper;

	@SingletonDependency
	MediaLogic mediaLogic;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// state

	MediaRec media;

	byte[] data;

	// hooks

	protected abstract
	byte[] getData (
			TaskLogger parentTaskLogger,
			MediaRec media);

	protected abstract
	String getMimeType (
			MediaRec media);

	// implementation

	@Override
	protected
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"prepare ()");

		) {

			media =
				mediaHelper.findRequired (
					requestContext.stuffIntegerRequired (
						"mediaId"));

			transform (
				taskLogger);

		}

	}

	protected
	void transform (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"transform ()");

		) {

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
								taskLogger,
								getData (
									taskLogger,
									media),
								getMimeType (
									media))),
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
								taskLogger,
								getData (
									taskLogger,
									media),
								getMimeType (
									media))),
						getMimeType (
							media));

			} else if (
				stringEqualSafe (
					rotate,
					"270")
			) {

				data =
					mediaLogic.writeImage (
						mediaLogic.rotateImage270 (
							mediaLogic.readImageRequired (
								taskLogger,
								getData (
									taskLogger,
									media),
								getMimeType (
									media))),
						getMimeType (
							media));

			} else {

				data =
					getData (
						taskLogger,
						media);

			}

		}

	}

	@Override
	protected
	void setHtmlHeaders (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setHtmlHeaders");

		) {

			requestContext.setHeader (
				"Content-Type",
				getMimeType (media));

			requestContext.setHeader (
				"Content-Length",
				Integer.toString (
					data.length));

		}

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
