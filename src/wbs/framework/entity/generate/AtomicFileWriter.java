package wbs.framework.entity.generate;

import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.stringFormatArray;

import java.io.File;
import java.io.IOException;

import lombok.NonNull;

import org.apache.commons.io.FileUtils;

import wbs.framework.utils.etc.FormatWriter;

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
			Object... arguments) {

		stringBuilder.append (
			stringFormatArray (
				arguments));

	}

	@Override
	public
	void close ()
		throws IOException {

		String newContents =
			stringBuilder.toString ();

		if (
			! contentHasChanged (
				newContents)
		) {
			return;
		}

		FileUtils.writeStringToFile (
			file,
			newContents);

	}

	private
	boolean contentHasChanged (
			String newContents)
		throws IOException {

		if (! file.exists ()) {
			return true;
		}

		String oldContents =
			FileUtils.readFileToString (
				file);

		return notEqual (
			oldContents,
			newContents);

	}

}
