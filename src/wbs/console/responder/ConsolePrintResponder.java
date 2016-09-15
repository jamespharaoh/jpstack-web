package wbs.console.responder;

import static wbs.utils.string.FormatWriterUtils.clearCurrentFormatWriter;
import static wbs.utils.string.FormatWriterUtils.setCurrentFormatWriter;

import java.io.IOException;

import lombok.NonNull;

import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.utils.string.FormatWriter;
import wbs.utils.string.WriterFormatWriter;

public abstract
class ConsolePrintResponder
	extends ConsoleResponder {

	// dependencies

	@SingletonDependency
	protected
	ConsoleRequestContext requestContext;

	// state

	protected
	FormatWriter formatWriter;

	// implenentation

	@Override
	protected
	void setup ()
		throws IOException {

		formatWriter =
			new WriterFormatWriter (
				requestContext.writer ())

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

	@Deprecated
	protected
	void printFormat (
			@NonNull Object ... arguments) {

		formatWriter.writeFormatArray (
			arguments);

	}

}
