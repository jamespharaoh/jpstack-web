package wbs.framework.web;

import java.io.IOException;
import java.io.PrintWriter;

import wbs.framework.utils.etc.StringFormatter;

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
			requestContext.writer ();

		super.setup ();

	}

	protected
	void printFormat (
			Object... args) {

		writer.print (
			StringFormatter.standard (args));

	}

}
