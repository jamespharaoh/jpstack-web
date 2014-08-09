package wbs.framework.web;

import java.io.IOException;

import javax.servlet.ServletException;

/**
 * A RequestHandler simply handles a request.
 *
 * These are delegated to by SimpleWebFile depending on the method.
 */
public
interface RequestHandler {

	public
	void handle ()
		throws
			ServletException,
			IOException;

}
