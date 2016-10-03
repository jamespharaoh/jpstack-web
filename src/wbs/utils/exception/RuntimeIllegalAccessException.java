package wbs.utils.exception;

import lombok.NonNull;

public
class RuntimeIllegalAccessException
	extends RuntimeException {

	public
	RuntimeIllegalAccessException (
			@NonNull IllegalAccessException cause) {

		super (
			cause);

	}

}
