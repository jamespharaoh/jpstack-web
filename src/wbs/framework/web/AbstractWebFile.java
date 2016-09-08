package wbs.framework.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import wbs.framework.component.annotations.SingletonDependency;

public
class AbstractWebFile
	implements WebFile {

	// singleton dependencies

	@SingletonDependency
	RequestContext requestContext;

	// implementation

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
