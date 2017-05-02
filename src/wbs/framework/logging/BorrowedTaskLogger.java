package wbs.framework.logging;

import lombok.NonNull;

public
class BorrowedTaskLogger
	implements TaskLogger {

	// state

	private final
	TaskLoggerImplementation taskLoggerImplementation;

	// constructors

	public
	BorrowedTaskLogger (
			@NonNull TaskLoggerImplementation taskLoggerImplementation) {

		this.taskLoggerImplementation =
			taskLoggerImplementation;

	}

	// implementation

	@Override
	public
	TaskLoggerImplementation taskLoggerImplementation () {

		return taskLoggerImplementation;

	}

}
