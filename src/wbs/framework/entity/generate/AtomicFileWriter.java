package wbs.framework.entity.generate;

import static wbs.framework.utils.etc.Misc.doNothing;
import static wbs.framework.utils.etc.StringUtils.stringFormatArray;
import static wbs.framework.utils.etc.StringUtils.stringNotEqualSafe;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import lombok.NonNull;
import wbs.framework.utils.etc.FormatWriter;
import wbs.framework.utils.etc.RuntimeIoException;

public
class AtomicFileWriter
	implements FormatWriter {

	// state

	File file;

	StringBuilder stringBuilder =
		new StringBuilder ();

	// implementation

	public
	AtomicFileWriter (
			@NonNull String filename) {

		file =
			new File (filename);

	}

	@Override
	public
	void writeFormat (
			@NonNull Object... arguments) {

		stringBuilder.append (
			stringFormatArray (
				arguments));

	}

	@Override
	public
	void writeFormatArray (
			@NonNull Object[] arguments) {

		stringBuilder.append (
			stringFormatArray (
				arguments));

	}

	@Override
	public
	void writeString (
			@NonNull String string) {

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
	void close () {

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
