package wbs.platform.exception.logic;

import com.google.common.base.Optional;

public
interface ExceptionLogic {

	void logSimple (
			String typeCode,
			String source,
			String summary,
			String dump,
			Optional<Integer> userId,
			Boolean fatal);

	void logThrowable (
			String typeCode,
			String source,
			Throwable throwable,
			Optional<Integer> userId,
			Boolean fatal);

	void logThrowableWithSummary (
			String typeCode,
			String source,
			String summary,
			Throwable throwable,
			Optional<Integer> userId,
			Boolean fatal);

}