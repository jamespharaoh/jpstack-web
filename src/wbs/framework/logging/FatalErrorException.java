package wbs.framework.logging;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
public
class FatalErrorException
	extends RuntimeException {

	@Getter
	TaskLogger taskLogger;

	public
	FatalErrorException (
			@NonNull TaskLogger taskLogger,
			@NonNull String message) {

		super (
			message);

		this.taskLogger =
			taskLogger;

	}

	public
	FatalErrorException (
			@NonNull TaskLogger taskLogger,
			@NonNull String message,
			@NonNull Throwable cause) {

		super (
			message,
			cause);

		this.taskLogger =
			taskLogger;

	}

}
