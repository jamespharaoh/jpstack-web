package wbs.utils.exception;

import lombok.NonNull;

public
class RuntimeInstantiationException
	extends RuntimeException {

	public
	RuntimeInstantiationException (
			@NonNull InstantiationException cause) {

		super (
			cause);

	}

}
