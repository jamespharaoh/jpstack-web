package wbs.framework.logging;

import com.google.common.base.Optional;

public
interface LogTarget {

	void writeToLog (
			TaskLogger parentTaskLogger,
			LogSeverity severity,
			String message,
			Optional <Throwable> exception);

	boolean debugEnabled ();

}