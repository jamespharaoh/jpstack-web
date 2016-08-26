package wbs.console.responder;

import java.io.IOException;
import java.io.PrintWriter;

import javax.inject.Inject;

import wbs.console.request.ConsoleRequestContext;
import wbs.framework.utils.formatwriter.FormatWriter;
import wbs.framework.utils.formatwriter.WriterFormatWriter;

public abstract
class ConsolePrintResponder
	extends ConsoleResponder {

	// dependencies

	@Inject
	protected
	ConsoleRequestContext requestContext;

	// state

	protected
	FormatWriter formatWriter;

	protected
	PrintWriter printWriter;

	// implenentation

	@Override
	protected
	void setup ()
		throws IOException {

		printWriter =
			requestContext.writer ();

		formatWriter =
			new WriterFormatWriter (
				printWriter);

	}

	protected
	void printFormat (
			Object... arguments) {

		formatWriter.writeFormatArray (
			arguments);

	}

}
