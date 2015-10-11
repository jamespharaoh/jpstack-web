package wbs.framework.utils.etc;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.io.Writer;

public
class FormatWriter {

	Writer writer;

	public
	FormatWriter (
			Writer writer) {

		this.writer = writer;

	}

	public
	void write (
			Object... arguments)
		throws IOException {

		writer.write (
			stringFormat (
				arguments));

	}

	public
	void close ()
		throws IOException {

		writer.close ();

	}

}
