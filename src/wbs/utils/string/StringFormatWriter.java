package wbs.utils.string;

import static wbs.utils.etc.Misc.doNothing;

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
	String indentString = "";

	@Getter @Setter
	long indentSize = 0;

	// state

	StringBuilder stringBuilder =
		new StringBuilder ();

	// implementation

	@Override
	public
	void writeString (
			@NonNull CharSequence string) {

		stringBuilder.append (
			string);

	}

	@Override
	public
	void writeCharacter (
			int character) {

		stringBuilder.appendCodePoint (
			character);

	}

	@Override
	public
	String toString () {

		return stringBuilder.toString ();

	}

	@Override
	public
	void close () {

		doNothing ();

	}

}
