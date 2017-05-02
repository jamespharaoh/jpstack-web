package wbs.framework.logging;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.NonNull;

public
interface TaskLoggerMethods {

	TaskLoggerImplementation taskLoggerImplementation ();

	default
	FatalErrorException fatalFormat (
			@NonNull CharSequence ... arguments) {

		return taskLoggerImplementation ().fatalFormat (
			arguments);

	}

	default
	FatalErrorException fatalFormatException (
			@NonNull Throwable exception,
			@NonNull CharSequence ... arguments) {

		return taskLoggerImplementation ().fatalFormatException (
			exception,
			arguments);

	}

	default
	void errorFormat (
			@NonNull CharSequence ... arguments) {

		taskLoggerImplementation ().errorFormat (
			arguments);

	}

	default
	void errorFormatException (
			@NonNull Throwable exception,
			@NonNull CharSequence ... arguments) {

		taskLoggerImplementation ().errorFormatException (
			exception,
			arguments);

	}

	default
	void firstErrorFormat (
			@NonNull CharSequence ... arguments) {

		taskLoggerImplementation ().firstErrorFormat (
			arguments);

	}

	default
	void warningFormat (
			@NonNull CharSequence ... arguments) {

		taskLoggerImplementation ().warningFormat (
			arguments);

	}

	default
	void warningFormatException (
			@NonNull Throwable exception,
			@NonNull CharSequence ... arguments) {

		taskLoggerImplementation ().warningFormatException (
			exception,
			arguments);

	}

	default
	void noticeFormat (
			@NonNull CharSequence ... arguments) {

		taskLoggerImplementation ().noticeFormat (
			arguments);

	}

	default
	void noticeFormatException (
			@NonNull Throwable exception,
			@NonNull CharSequence ... arguments) {

		taskLoggerImplementation ().noticeFormatException (
			exception,
			arguments);

	}

	default
	void logicFormat (
			@NonNull CharSequence ... arguments) {

		taskLoggerImplementation ().logicFormat (
			arguments);

	}

	default
	void logicFormatException (
			@NonNull Throwable exception,
			@NonNull CharSequence ... arguments) {

		taskLoggerImplementation ().logicFormatException (
			exception,
			arguments);

	}

	default
	void traceFormat (
			@NonNull CharSequence ... arguments) {

		taskLoggerImplementation ().traceFormat (
			arguments);

	}

	default
	void traceFormatException (
			@NonNull Throwable exception,
			@NonNull CharSequence ... arguments) {

		taskLoggerImplementation ().traceFormatException (
			exception,
			arguments);

	}

	default
	void debugFormat (
			@NonNull CharSequence ... arguments) {

		taskLoggerImplementation ().debugFormat (
			arguments);

	}

	default
	void debugFormatException (
			@NonNull Throwable exception,
			@NonNull CharSequence ... arguments) {

		taskLoggerImplementation ().debugFormatException (
			exception,
			arguments);

	}

	default
	boolean debugEnabled () {

		return taskLoggerImplementation ().debugEnabled ();

	}

	default
	RuntimeException makeException () {

		return taskLoggerImplementation ().makeException ();

	}

	default
	RuntimeException makeException (
			@NonNull Supplier <RuntimeException> exceptionSupplier) {

		return taskLoggerImplementation ().makeException (
			exceptionSupplier);

	}

	default
	TaskLoggerImplementation findRoot () {

		return taskLoggerImplementation ().findRoot ();

	}

	default
	boolean errors () {

		return taskLoggerImplementation ().errors ();

	}

	default
	long errorCount () {

		return taskLoggerImplementation ().errorCount ();

	}

	default <Type>
	Type wrap (
			@NonNull Function <TaskLogger, Type> function) {

		return taskLoggerImplementation ().wrap (
			function);

	}

	default
	void wrap (
			@NonNull Consumer <TaskLogger> function) {

		taskLoggerImplementation ().wrap (
			function);

	}

	default
	BorrowedTaskLogger borrow () {

		return new BorrowedTaskLogger (
			this.taskLoggerImplementation ());

	}

}
