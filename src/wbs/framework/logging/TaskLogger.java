package wbs.framework.logging;

import java.util.List;

import org.joda.time.Instant;

import wbs.utils.etc.ImplicitArgument;

public
interface TaskLogger
	extends
		TaskLogEvent,
		TaskLoggerMethods {

	// task log event interface

	@Override
	default
	Long eventId () {
		return taskLoggerImplementation ().eventId ();
	}

	@Override
	default
	LogSeverity eventSeverity () {
		return taskLoggerImplementation ().eventSeverity ();
	}

	@Override
	default
	String eventText () {
		return taskLoggerImplementation ().eventText ();
	}

	@Override
	default
	Instant eventStartTime () {
		return taskLoggerImplementation ().eventStartTime ();
	}

	@Override
	default
	Instant eventEndTime () {
		return taskLoggerImplementation ().eventEndTime ();
	}

	@Override
	default
	List <TaskLogEvent> eventChildren () {

		return taskLoggerImplementation ().eventChildren ();

	}

	// implicit argument

	ImplicitArgument <CloseableTaskLogger, BorrowedTaskLogger>
		implicitArgument =
			new ImplicitArgument <> (
				TaskLogger::borrow);

}
