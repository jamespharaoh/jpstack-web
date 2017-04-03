package wbs.console.responder;

import static wbs.utils.string.FormatWriterUtils.clearCurrentFormatWriter;
import static wbs.utils.string.FormatWriterUtils.setCurrentFormatWriter;

import lombok.NonNull;

import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.utils.string.FormatWriter;
import wbs.utils.string.WriterFormatWriter;

public abstract
class ConsolePrintResponder
	extends ConsoleResponder {

	// dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	protected
	ConsoleRequestContext requestContext;

	// state

	protected
	FormatWriter formatWriter;

	// implenentation

	@SuppressWarnings ("resource")
	@Override
	protected
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		formatWriter =
			new WriterFormatWriter (
				requestContext.printWriter ())

			.indentString (
				"  ");

		setCurrentFormatWriter (
			formatWriter);

	}

	@Override
	protected
	void cleanup () {

		clearCurrentFormatWriter (
			formatWriter);

	}

}
