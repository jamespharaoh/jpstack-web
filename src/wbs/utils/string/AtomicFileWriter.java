package wbs.utils.string;

import static wbs.utils.string.StringUtils.stringFormatArray;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;

import java.io.File;
import java.io.IOException;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.io.FileUtils;

import wbs.utils.io.RuntimeIoException;

@Accessors (fluent = true)
public
class AtomicFileWriter
	implements FormatWriter {

	// properties

	@Getter @Setter
	String indentString = "\t";

	@Getter @Setter
	long indentSize = 0;

	// state

	File file;

	StringBuilder stringBuilder =
		new StringBuilder ();

	// implementation

	public
	AtomicFileWriter (
			@NonNull String filename) {

		file =
			new File (
				filename);

	}

	@Override
	public
	void writeFormat (
			@NonNull CharSequence ... arguments) {

		stringBuilder.append (
			stringFormatArray (
				arguments));

	}

	@Override
	public
	void writeFormatArray (
			@NonNull CharSequence[] arguments) {

		stringBuilder.append (
			stringFormatArray (
				arguments));

	}

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

		stringBuilder.append (
			(char) character);

	}

	@Override
	public
	void commit () {

		String newContents =
			stringBuilder.toString ();

		if (
			! contentHasChanged (
				newContents)
		) {
			return;
		}

		try {

			FileUtils.writeStringToFile (
				file,
				newContents);

		} catch (IOException exception) {

			throw new RuntimeIoException (
				exception);

		}

	}

	@Override
	public
	void close () {

	}

	private
	boolean contentHasChanged (
			@NonNull String newContents) {

		if (! file.exists ()) {

			return true;
		}

		try {

			String oldContents =
				FileUtils.readFileToString (
					file);

			return stringNotEqualSafe (
				oldContents,
				newContents);

		} catch (IOException exception) {

			throw new RuntimeIoException (
				exception);

		}

	}

}
