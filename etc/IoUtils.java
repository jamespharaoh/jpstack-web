package wbs.utils.etc;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import lombok.NonNull;

import wbs.utils.io.RuntimeIoException;

public
class IoUtils {

	public static
	void writeBytes (
			@NonNull OutputStream target,
			@NonNull byte[] bytes) {

		try {

			target.write (
				bytes);

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

	public static
	void writeString (
			@NonNull Writer target,
			@NonNull String string) {

		try {

			target.write (
				string);

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

	public static
	void writeString (
			@NonNull Writer target,
			@NonNull CharSequence charSequence) {

		try {

			target.append (
				charSequence);

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

	public static
	void writeByte (
			@NonNull OutputStream target,
			int value) {

		try {

			target.write (
				value);

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

}
