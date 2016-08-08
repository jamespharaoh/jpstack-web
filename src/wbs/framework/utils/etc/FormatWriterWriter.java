package wbs.framework.utils.etc;

import static wbs.framework.utils.etc.Misc.doNothing;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.io.IOException;
import java.io.Writer;

import lombok.NonNull;

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
			Object... arguments) {

		try {

			writer.write (
				stringFormat (
					arguments));

		} catch (IOException exception) {

			throw new RuntimeIoException (exception);

		}

	}

	@Override
	public
	void writeFormatArray (
			Object[] arguments) {

		try {

			writer.write (
				stringFormat (
					arguments));

		} catch (IOException exception) {

			throw new RuntimeIoException (exception);

		}

	}

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

	@Override
	public
	void indent () {

		doNothing ();

	}

	@Override
	public
	void unindent () {

		doNothing ();

	}

}
