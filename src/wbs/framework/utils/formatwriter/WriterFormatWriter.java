package wbs.framework.utils.formatwriter;

import java.io.IOException;
import java.io.Writer;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.utils.etc.RuntimeIoException;

@Accessors (fluent = true)
public
class WriterFormatWriter
	implements FormatWriter {

	// properties

	@Getter @Setter
	String indentString;

	@Getter @Setter
	long indentSize;

	// state

	Writer writer;

	// constructors

	public
	WriterFormatWriter (
			Writer writer) {

		this.writer = writer;

	}

	// implementation

	@Override
	public
	void writeString (
			@NonNull String string) {

		try {

			writer.write (
				string);

		} catch (IOException exception) {

			throw new RuntimeIoException (exception);

		}

	}

	@Override
	public
	void writeCharacter (
			int character) {

		try {

			writer.append (
				(char) character);

		} catch (IOException exception) {

			throw new RuntimeIoException (exception);

		}

	}

	@Override
	public
	void close () {

		try {

			writer.close ();

		} catch (IOException exception) {

			throw new RuntimeIoException (exception);

		}

	}

}
