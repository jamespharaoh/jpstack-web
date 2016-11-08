package wbs.framework.logging;

import com.google.common.base.Optional;

import org.apache.log4j.Logger;

public
interface LogTarget {

	void writeToLog (
			LogSeverity severity,
			String message,
			Optional <Throwable> exception);

	LogTarget nest (
			Logger logger);

}