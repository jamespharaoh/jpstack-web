package wbs.utils.io;

import java.io.IOException;

public
class RuntimeIoException
	extends RuntimeException {

	public
	RuntimeIoException (
			IOException cause) {

		super (
			cause);

	}

}
