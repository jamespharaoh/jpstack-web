package wbs.framework.exception;

import wbs.framework.record.Record;

import com.google.common.base.Optional;

public
interface ExceptionLogger {

	Record<?> logSimple (
			String typeCode,
			String source,
			String summary,
			String dump,
			Optional<Integer> userId,
			Boolean fatal);

	Record<?> logThrowable (
			String typeCode,
			String source,
			Throwable throwable,
			Optional<Integer> userId,
			Boolean fatal);

	Record<?> logThrowableWithSummary (
			String typeCode,
			String source,
			String summary,
			Throwable throwable,
			Optional<Integer> userId,
			Boolean fatal);

}
