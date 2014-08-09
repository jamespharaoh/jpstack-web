package wbs.framework.web;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

/**
 * Simple base class for WebFile implementations.
 *
 * Sends a METHOD_NOT_ALLOWED error for all requests.
 */
public
class AbstractWebFile
	implements WebFile {

	@Inject
	RequestContext requestContext;

	@Override
	public
	void doGet ()
		throws
			ServletException,
			IOException {

		requestContext.sendError (
			HttpServletResponse.SC_METHOD_NOT_ALLOWED,
			requestContext.requestUri ());

	}

	@Override
	public
	void doPost ()
		throws
			ServletException,
			IOException {

		requestContext.sendError (
			HttpServletResponse.SC_METHOD_NOT_ALLOWED,
			requestContext.requestUri ());

	}

	@Override
	public
	void doOptions ()
		throws
			ServletException,
			IOException {

	}

}
