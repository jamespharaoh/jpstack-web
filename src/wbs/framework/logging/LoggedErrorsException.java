package wbs.framework.logging;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
public
class LoggedErrorsException
	extends RuntimeException {

	@Getter
	TaskLogger taskLogger;

	public
	LoggedErrorsException (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String message) {

		super (
			message);

		this.taskLogger =
			taskLogger;

	}

}
