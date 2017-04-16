package wbs.utils.etc;

import java.io.IOException;
import java.io.OutputStream;

import lombok.NonNull;

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

			throw new RuntimeException (
				ioException);

		}

	}

}
