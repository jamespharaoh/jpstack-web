package wbs.web.servlet;

import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.string.StringUtils.joinWithoutSeparator;

import java.io.IOException;

import javax.inject.Named;
import javax.servlet.ServletException;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.web.exceptions.ExternalRedirectException;
import wbs.web.exceptions.HttpNotFoundException;
import wbs.web.file.WebFile;
import wbs.web.handler.WebExceptionHandler;
import wbs.web.handler.WebNotFoundHandler;
import wbs.web.pathhandler.PathHandler;

@SingletonComponent ("pathHandlerServlet")
public
class PathHandlerServlet
	extends WbsServlet {

	// singleton dependencies

	@SingletonDependency
	@Named ("rootPathHandler")
	PathHandler pathHandler;

	@SingletonDependency
	WebExceptionHandler exceptionHandler;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	WebNotFoundHandler notFoundHandler;

	// implementation

	@Override
	public
	void init ()
		throws ServletException {

		super.init ();

	}

	@Override
	protected
	WebFile processPath (
			@NonNull TaskLogger parentTaskLogger)
		throws ServletException {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"processPath");

		// get the full path (servlet path & path info)

		String path =
			joinWithoutSeparator (
				requestContext.servletPath (),
				ifThenElse (
					optionalIsPresent (
						requestContext.pathInfo ()),
					() -> requestContext.pathInfo ().get (),
					() -> ""));

		// and call the relevant pathhandler

		return pathHandler.processPath (
			taskLogger,
			path);

	}

	@Override
	protected
	void handleException (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Throwable exception)
		throws
			ServletException,
			IOException {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"handleException");

		if (exception instanceof ExternalRedirectException) {

			ExternalRedirectException redirectException =
				(ExternalRedirectException) exception;

			requestContext.sendRedirect (
				redirectException.getLocation ());

		} else if (exception instanceof HttpNotFoundException) {

			throw (HttpNotFoundException) exception;

		} else if (exceptionHandler != null) {

			exceptionHandler.handleException (
				taskLogger,
				exception);

		} else {

			super.handleException (
				taskLogger,
				exception);

		}

	}

	@Override
	protected
	void handleNotFound (
			@NonNull TaskLogger parentTaskLogger)
		throws
			ServletException,
			IOException {

		if (notFoundHandler != null) {

			notFoundHandler.handleNotFound (
				parentTaskLogger);

		} else {

			super.handleNotFound (
				parentTaskLogger);

		}

	}

}
