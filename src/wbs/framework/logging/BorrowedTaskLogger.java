package wbs.framework.logging;

import lombok.NonNull;

public
class BorrowedTaskLogger
	implements
		TaskLogger,
		TaskLoggerDefault {

	// state

	private final
	RealTaskLogger realTaskLogger;

	// constructors

	public
	BorrowedTaskLogger (
			@NonNull RealTaskLogger realTaskLogger) {

		this.realTaskLogger =
			realTaskLogger;

	}

	// implementation

	@Override
	public
	RealTaskLogger realTaskLogger () {
		return realTaskLogger;
	}

	@Override
	public
	ParentTaskLogger parentTaskLogger () {
		return realTaskLogger;
	}

}
