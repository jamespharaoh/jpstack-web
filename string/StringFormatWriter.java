package wbs.utils.string;

import static wbs.utils.etc.Misc.doNothing;

import java.io.StringWriter;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
public
class StringFormatWriter
	implements FormatWriter {

	// properties

	@Getter @Setter
	String indentString;

	@Getter @Setter
	long indentSize;

	// state

	StringWriter stringWriter =
		new StringWriter ();

	// implementation

	@Override
	public
	void writeString (
			@NonNull String string) {

		stringWriter.write (
			string);

	}

	@Override
	public
	void writeCharacter (
			int character) {

		stringWriter.write (
			character);

	}

	@Override
	public
	String toString () {

		return stringWriter.toString ();

	}

	@Override
	public
	void close () {

		doNothing ();

	}

}
