package wbs.framework.logging;

import static wbs.utils.string.StringUtils.stringFormatLazyArray;

public
interface LoggingLogicMethods {

	LogContext findOrCreateLogContext (
			CharSequence staticContextName);

	default
	LogContext findOrCreateLogContextFormat (
			CharSequence ... staticContextNameArguments) {

		return findOrCreateLogContext (
			stringFormatLazyArray (
				staticContextNameArguments));

	}

}
