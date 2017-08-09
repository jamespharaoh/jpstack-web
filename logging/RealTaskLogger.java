package wbs.framework.logging;

import com.google.common.base.Optional;

import wbs.utils.etc.SafeCloseable;

public
interface RealTaskLogger
	extends
		ParentTaskLogger,
		SafeCloseable,
		TaskLogger,
		TaskLogEvent,
		TaskLoggerMethods {

	Optional <ParentTaskLogger> parentOptional ();

}
