package wbs.web.handler;

import java.io.IOException;

import javax.servlet.ServletException;

import wbs.framework.logging.TaskLogger;

/**
 * A RequestHandler simply handles a request.
 *
 * These are delegated to by SimpleWebFile depending on the method.
 */
public
interface RequestHandler {

	public
	void handle (
			TaskLogger taskLogger)
		throws
			ServletException,
			IOException;

}
