package wbs.web.handler;

import wbs.framework.logging.TaskLogger;

public
interface WebExceptionHandler {

	void handleException (
			TaskLogger taskLogger,
			Throwable exception);

}
