package wbs.framework.logging;

import com.google.common.base.Optional;

public
interface LogTarget {

	void writeToLog (
			LogSeverity severity,
			String message,
			Optional <Throwable> exception);

	boolean debugEnabled ();

}