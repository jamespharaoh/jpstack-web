package wbs.platform.exception.logic;

import com.google.common.base.Optional;

import wbs.platform.exception.model.ExceptionLogRec;
import wbs.platform.exception.model.ExceptionResolution;

public
interface ExceptionLogLogic {

	ExceptionLogRec logException (
			String typeCode,
			String source,
			String summary,
			String dump,
			Optional<Integer> userId,
			ExceptionResolution resolution);

}