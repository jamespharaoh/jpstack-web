package wbs.platform.exception.logic;

import com.google.common.base.Optional;

import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.TaskLogger;

import wbs.platform.exception.model.ExceptionLogRec;

public
interface ExceptionLogLogic {

	ExceptionLogRec logException (
			TaskLogger parentTaskLogger,
			String typeCode,
			String source,
			String summary,
			String dump,
			Optional <Long> userId,
			GenericExceptionResolution resolution);

}