package wbs.utils.string;

import static wbs.utils.string.StringUtils.stringFormatArray;

public
interface FormatWriter {

	// necessary methods

	void writeString (
			String string);

	void writeCharacter (
			int character);

	void close ();

	String indentString ();

	FormatWriter indentString (
			String indentString);

	long indentSize ();

	FormatWriter indentSize (
			long indentSize);

	// default methods

	default
	void writeFormat (
			Object ... arguments) {

		writeString (
			stringFormatArray (
				arguments));

	}

	default
	void writeFormatArray (
			Object[] arguments) {

		writeString (
			stringFormatArray (
				arguments));

	}

	default
	void writeLineFormat (
			Object ... arguments) {

		writeIndent ();

		writeString (
			stringFormatArray (
				arguments));

		writeNewline ();

	}

	default
	void writeLineFormatArray (
			Object[] arguments) {

		writeIndent ();

		writeString (
			stringFormatArray (
				arguments));

		writeNewline ();

	}

	default
	void writeNewline () {

		writeCharacter (
			'\n');

	}

	default
	void writeIndent () {

		for (
			long counter = 0l;
			counter < indentSize ();
			counter ++
		) {

			writeString (
				indentString ());

		}

	}

	default
	void writeNewlineAndIndent () {

		writeNewline ();

		writeIndent ();

	}

	default
	void increaseIndent () {

		indentSize (
			indentSize () + 1);

	}

	default
	void decreaseIndent () {

		indentSize (
			indentSize () - 1);

	}

}
