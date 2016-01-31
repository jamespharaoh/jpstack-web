package wbs.platform.exception.logic;

import com.google.common.base.Optional;

import wbs.framework.exception.GenericExceptionResolution;
import wbs.platform.exception.model.ExceptionLogRec;

public
interface ExceptionLogLogic {

	ExceptionLogRec logException (
			String typeCode,
			String source,
			String summary,
			String dump,
			Optional<Integer> userId,
			GenericExceptionResolution resolution);

}