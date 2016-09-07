package wbs.framework.web;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;

import wbs.framework.component.annotations.SingletonComponent;

@SingletonComponent ("pathHandlerServlet")
public
class PathHandlerServlet
	extends WbsServlet {

	// dependencies

	@Inject
	@Named ("rootPathHandler")
	PathHandler pathHandler;

	@Inject
	WebExceptionHandler exceptionHandler;

	@Inject
	WebNotFoundHandler notFoundHandler;

	// implementation

	@Override
	public
	void init ()
		throws ServletException {

		super.init ();

	}

	@Override
	protected WebFile processPath ()
		throws ServletException {

		// get the full path (servlet path & path info)

		String path = ""
			+ requestContext.servletPath ()
			+ (requestContext.pathInfo () != null
				? requestContext.pathInfo ()
				: "");

		// and call the relevant pathhandler

		return pathHandler.processPath (
			path);

	}

	@Override
	protected
	void handleException (
			Throwable exception)
		throws
			ServletException,
			IOException {

		if (exception instanceof ExternalRedirectException) {

			ExternalRedirectException redirectException =
				(ExternalRedirectException) exception;

			requestContext.sendRedirect (
				redirectException.getLocation ());

		} else if (exception instanceof PageNotFoundException) {

			throw (PageNotFoundException) exception;

		} else if (exceptionHandler != null) {

			exceptionHandler.handleException (
				exception);

		} else {

			super.handleException (
				exception);

		}

	}

	@Override
	protected
	void handleNotFound ()
		throws
			ServletException,
			IOException {

		if (notFoundHandler != null) {

			notFoundHandler.handleNotFound ();

		} else {

			super.handleNotFound ();

		}

	}

}
