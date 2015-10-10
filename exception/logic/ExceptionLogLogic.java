package wbs.platform.exception.logic;

import wbs.platform.exception.model.ExceptionLogRec;

import com.google.common.base.Optional;

public
interface ExceptionLogLogic {

	ExceptionLogRec logException (
			String typeCode,
			String source,
			String summary,
			String dump,
			Optional<Integer> userId,
			Boolean fatal);

}