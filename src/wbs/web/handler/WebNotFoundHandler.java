package wbs.web.handler;

import java.io.IOException;

import javax.servlet.ServletException;

import wbs.framework.logging.TaskLogger;

/**
 * Simple abstraction used by "PathHandlerServlet" to handle requests when a
 * PathHandler returns null for a path.
 */
public
interface WebNotFoundHandler {

	void handleNotFound (
			TaskLogger taskLogger)
		throws
			ServletException,
			IOException;

}
