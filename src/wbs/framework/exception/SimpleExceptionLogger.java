package wbs.framework.exception;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("simpleExceptionLogger")
public
class SimpleExceptionLogger
	implements ExceptionLogger {

	// singleton dependencies

	@SingletonDependency
	ExceptionUtils exceptionLogic;

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	Record <?> logSimple (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String typeCode,
			@NonNull String source,
			@NonNull String summary,
			@NonNull String dump,
			@NonNull Optional<Long> userId,
			@NonNull GenericExceptionResolution resolution) {

		parentTaskLogger.errorFormat (
			"%s: %s",
			source,
			summary);

		return null;

	}

	@Override
	public
	Record<?> logThrowable (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String typeCode,
			@NonNull String source,
			@NonNull Throwable throwable,
			@NonNull Optional<Long> userId,
			@NonNull GenericExceptionResolution resolution) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"logThrowable");

		parentTaskLogger.errorFormatException (
			throwable,
			"%s: %s",
			source,
			exceptionLogic.throwableSummary (
				taskLogger,
				throwable));

		return null;

	}

	@Override
	public
	Record <?> logThrowableWithSummary (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String typeCode,
			@NonNull String source,
			@NonNull String summary,
			@NonNull Throwable throwable,
			@NonNull Optional<Long> userId,
			@NonNull GenericExceptionResolution resolution) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"logThrowableWithSummary");

		parentTaskLogger.errorFormatException (
			throwable,
			"%s: %s",
			source,
			exceptionLogic.throwableSummary (
				taskLogger,
				throwable));

		return null;

	}


}
