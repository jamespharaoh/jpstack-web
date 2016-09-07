package wbs.console.responder;

import java.io.IOException;
import java.io.PrintWriter;

import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.utils.formatwriter.FormatWriter;
import wbs.framework.utils.formatwriter.WriterFormatWriter;

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
