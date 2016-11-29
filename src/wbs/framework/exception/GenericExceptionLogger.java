package wbs.framework.exception;

import com.google.common.base.Optional;

import wbs.framework.entity.record.Record;

public
interface GenericExceptionLogger <Resolution> {

	Record <?> logSimple (
			String typeCode,
			String source,
			String summary,
			String dump,
			Optional <Long> userId,
			Resolution resolution);

	Record <?> logThrowable (
			String typeCode,
			String source,
			Throwable throwable,
			Optional <Long> userId,
			Resolution resolution);

	Record <?> logThrowableWithSummary (
			String typeCode,
			String source,
			String summary,
			Throwable throwable,
			Optional <Long> userId,
			Resolution resolution);

}
