package wbs.web.responder;

import java.io.IOException;
import java.io.PrintWriter;

import lombok.NonNull;

import wbs.utils.string.StringFormatter;

@Deprecated
public abstract
class PrintResponder
	extends AbstractResponder {

	protected
	PrintWriter writer;

	@Override
	protected
	void setup ()
		throws IOException {

		writer =
			requestContext.printWriter ();

		super.setup ();

	}

	protected
	void printFormat (
			@NonNull Object ... args) {

		writer.print (
			StringFormatter.standard (
				args));

	}

}
