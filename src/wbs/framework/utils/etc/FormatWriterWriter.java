package wbs.framework.utils.etc;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.io.Writer;

public
class FormatWriterWriter
	implements FormatWriter {

	Writer writer;

	public
	FormatWriterWriter (
			Writer writer) {

		this.writer = writer;

	}

	@Override
	public
	void writeFormat (
			Object... arguments)
		throws IOException {

		writer.write (
			stringFormat (
				arguments));

	}

	@Override
	public
	void close ()
		throws IOException {

		writer.close ();

	}

}
