package wbs.platform.media.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;

import java.io.IOException;
import java.io.OutputStream;

import lombok.NonNull;

import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ConsoleResponder;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.media.model.MediaRec;

import wbs.utils.io.RuntimeIoException;

@PrototypeComponent ("mediaAudioResponder")
public
class MediaAudioResponder
	extends ConsoleResponder {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaConsoleHelper mediaHelper;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	OutputStream out;
	byte[] data;

	@Override
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		out =
			requestContext.outputStream ();

	}

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		MediaRec media =
			mediaHelper.findFromContextRequired ();

		data =
			media.getContent ().getData ();

	}

	@Override
	public
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
				"audio/mpeg");

			requestContext.setHeader (
				"Content-Length",
				integerToDecimalString (
					data.length));

		}

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
