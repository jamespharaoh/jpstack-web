package wbs.framework.activitymanager;

import lombok.NonNull;

public
class RuntimeExceptionWithTask
	extends RuntimeException
	implements ExceptionWithTask {

	// state

	private
	Task task;

	// constructors

	public
	RuntimeExceptionWithTask (
			@NonNull Task task) {

		this.task =
			task;

	}

	public
	RuntimeExceptionWithTask (
			@NonNull Task task,
			@NonNull String message) {

		super (
			message);

		this.task =
			task;

	}

	public
	RuntimeExceptionWithTask (
			@NonNull Task task,
			@NonNull Throwable cause) {

		super (
			cause);

		this.task =
			task;

	}

	public
	RuntimeExceptionWithTask (
			@NonNull Task task,
			@NonNull String message,
			@NonNull Throwable cause) {

		super (
			message,
			cause);

		this.task =
			task;

	}

	// property accessors

	@Override
	public
	Task task () {

		return task;

	}

}
