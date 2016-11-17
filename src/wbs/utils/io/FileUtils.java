package wbs.utils.io;

import static wbs.utils.string.StringUtils.stringFormatArray;

import java.io.File;
import java.io.IOException;

import lombok.NonNull;

public
class FileUtils {

	public static
	boolean fileExists (
			@NonNull String filename) {

		File file =
			new File (
				filename);

		return file.exists ();

	}

	public static
	boolean fileExistsFormat (
			@NonNull String ... arguments) {

		return fileExists (
			stringFormatArray (
				arguments));

	}

	public static
	void directoryCreateWithParents (
			@NonNull String directoryName) {

		try {

			org.apache.commons.io.FileUtils.forceMkdir (
				new File (
					directoryName));

		} catch (IOException exception) {

			throw new RuntimeIoException (
				exception);

		}

	}

	public static
	void directoryCreateWithParentsFormat (
			@NonNull String ... arguments) {

		directoryCreateWithParents (
			stringFormatArray (
				arguments));

	}

	public static
	void deleteDirectory (
			@NonNull String path) {

		try {

			org.apache.commons.io.FileUtils.deleteDirectory (
				new File (
					path));

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

	public static
	void forceMkdir (
			@NonNull String path) {

		try {

			org.apache.commons.io.FileUtils.forceMkdir (
				new File (
					path));

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

}