package wbs.framework.utils.etc;

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
