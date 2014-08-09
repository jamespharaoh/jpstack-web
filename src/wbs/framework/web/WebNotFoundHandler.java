package wbs.framework.web;

import java.io.IOException;

import javax.servlet.ServletException;

/**
 * Simple abstraction used by "PathHandlerServlet" to handle requests when a
 * PathHandler returns null for a path.
 */
public
interface WebNotFoundHandler {

	void handleNotFound ()
		throws
			ServletException,
			IOException;

}
