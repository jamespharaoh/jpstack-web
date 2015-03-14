package wbs.platform.exception.logic;

import wbs.platform.exception.model.ExceptionLogRec;

import com.google.common.base.Optional;

public
interface ExceptionLogic {

	ExceptionLogRec logSimple (
			String typeCode,
			String source,
			String summary,
			String dump,
			Optional<Integer> userId,
			Boolean fatal);

	ExceptionLogRec logThrowable (
			String typeCode,
			String source,
			Throwable throwable,
			Optional<Integer> userId,
			Boolean fatal);

	ExceptionLogRec logThrowableWithSummary (
			String typeCode,
			String source,
			String summary,
			Throwable throwable,
			Optional<Integer> userId,
			Boolean fatal);

	String throwableSummary (
			Throwable throwable);

	String throwableDump (
			Throwable throwable);

}