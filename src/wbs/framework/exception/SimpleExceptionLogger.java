package wbs.framework.exception;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import com.google.common.base.Optional;

import wbs.framework.record.Record;

@Log4j
public
class SimpleExceptionLogger
	implements ExceptionLogger {

	// dependencies

	@Inject
	ExceptionUtils exceptionLogic;

	// implementation

	@Override
	public
	Record<?> logSimple (
			@NonNull String typeCode,
			@NonNull String source,
			@NonNull String summary,
			@NonNull String dump,
			@NonNull Optional<Long> userId,
			@NonNull GenericExceptionResolution resolution) {

		log.error (
			stringFormat (
				"%s: %s",
				source,
				summary));

		return null;

	}

	@Override
	public
	Record<?> logThrowable (
			@NonNull String typeCode,
			@NonNull String source,
			@NonNull Throwable throwable,
			@NonNull Optional<Long> userId,
			@NonNull GenericExceptionResolution resolution) {

		log.error (
			stringFormat (
				"%s: %s",
				source,
				exceptionLogic.throwableSummary (
					throwable)),
			throwable);

		return null;

	}

	@Override
	public
	Record<?> logThrowableWithSummary (
			@NonNull String typeCode,
			@NonNull String source,
			@NonNull String summary,
			@NonNull Throwable throwable,
			@NonNull Optional<Long> userId,
			@NonNull GenericExceptionResolution resolution) {

		log.error (
			stringFormat (
				"%s: %s",
				source,
				exceptionLogic.throwableSummary (
					throwable)),
			throwable);

		return null;

	}


}
