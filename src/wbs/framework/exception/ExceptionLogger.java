package wbs.framework.exception;

import com.google.common.base.Optional;

import wbs.framework.record.Record;

public
interface ExceptionLogger {

	Record<?> logSimple (
			String typeCode,
			String source,
			String summary,
			String dump,
			Optional<Integer> userId,
			Resolution resolution);

	Record<?> logThrowable (
			String typeCode,
			String source,
			Throwable throwable,
			Optional<Integer> userId,
			Resolution resolution);

	Record<?> logThrowableWithSummary (
			String typeCode,
			String source,
			String summary,
			Throwable throwable,
			Optional<Integer> userId,
			Resolution resolution);

	public static
	enum Resolution {

		tryAgainNow,
		tryAgainLater,
		ignoreWithUserWarning,
		ignoreWithThirdPartyWarning,
		ignoreWithLoggedWarning,
		ignoreWithNoWarning,
		fatalError;

	}

}
