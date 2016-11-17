package wbs.web.handler;

import java.io.IOException;

import javax.servlet.ServletException;

import wbs.framework.logging.TaskLogger;

public
interface WebExceptionHandler {

	void handleException (
			TaskLogger taskLogger,
			Throwable exception)
		throws
			ServletException,
			IOException;

}
