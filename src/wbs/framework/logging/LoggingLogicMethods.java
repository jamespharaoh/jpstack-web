package wbs.framework.logging;

import static wbs.utils.string.StringUtils.stringFormatArray;

public
interface LoggingLogicMethods {

	Long nextEventId ();

	LogContext findOrCreateLogContext (
			String staticContextName);

	default
	LogContext findOrCreateLogContextFormat (
			String ... staticContextNameArguments) {

		return findOrCreateLogContext (
			stringFormatArray (
				staticContextNameArguments));

	}

	void rootTaskBegin (
			TaskLogger taskLogger);

	void rootTaskEnd (
			TaskLogger taskLogger);

}
