package wbs.platform.media.console;

import static wbs.utils.etc.IoUtils.writeBytes;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;

import java.io.OutputStream;

import lombok.NonNull;

import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ConsoleResponder;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.media.model.MediaRec;

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
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"setup");

		) {

			out =
				requestContext.outputStream ();

		}

	}

	@Override
	public
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			MediaRec media =
				mediaHelper.findFromContextRequired (
					transaction);

			data =
				media.getContent ().getData ();

		}

	}

	@Override
	public
	void setHtmlHeaders (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"setHtmlHeaders");

		) {

			requestContext.contentType (
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
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"render");

		) {

			writeBytes (
				out,
				data);

		}

	}

}
