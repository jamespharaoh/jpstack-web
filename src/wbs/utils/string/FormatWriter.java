package wbs.utils.string;

import static wbs.utils.string.StringUtils.stringFormatArray;
import static wbs.utils.string.StringUtils.stringIsNotEmpty;

import java.io.Closeable;

import lombok.NonNull;

public
interface FormatWriter
	extends Closeable {

	// necessary methods

	void writeString (
			String string);

	void writeCharacter (
			int character);

	default
	void commit () {
		close ();
	}

	@Override
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
			String ... arguments) {

		writeString (
			stringFormatArray (
				arguments));

	}

	default
	void writeFormatArray (
			String [] arguments) {

		writeString (
			stringFormatArray (
				arguments));

	}

	default
	void writeLineFormat (
			@NonNull String ... arguments) {

		String lineContent =
			stringFormatArray (
				arguments);

		if (
			stringIsNotEmpty (
				lineContent)
		) {

			writeIndent ();

			writeString (
				lineContent);

			writeNewline ();

		}

	}

	default
	void writeLineFormatIncreaseIndent (
			@NonNull String ... arguments) {

		writeIndent ();

		writeString (
			stringFormatArray (
				arguments));

		writeNewline ();

		increaseIndent ();

	}

	default
	void writeLineFormatDecreaseIndent (
			@NonNull String ... arguments) {

		decreaseIndent ();

		writeIndent ();

		writeString (
			stringFormatArray (
				arguments));

		writeNewline ();

	}

	default
	void writeLineFormatDecreaseIncreaseIndent (
			@NonNull String ... arguments) {

		decreaseIndent ();

		writeIndent ();

		writeString (
			stringFormatArray (
				arguments));

		writeNewline ();

		increaseIndent ();

	}

	default
	void writeLineFormatArray (
			String [] arguments) {

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

	@SuppressWarnings ("resource")
	default
	StringFormatWriter stringBuffer () {

		return new StringFormatWriter ()

			.indentString (
				indentString ())

			.indentSize (
				indentSize ());

	}

}
