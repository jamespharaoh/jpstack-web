package wbs.framework.logging;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
public
class FatalErrorException
	extends RuntimeException {

	@Getter
	TaskLogger parentTaskLogger;

	public
	FatalErrorException (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String message) {

		super (
			message);

		this.parentTaskLogger =
			parentTaskLogger;

	}

	public
	FatalErrorException (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String message,
			@NonNull Throwable cause) {

		super (
			message,
			cause);

		this.parentTaskLogger =
			parentTaskLogger;

	}

}
