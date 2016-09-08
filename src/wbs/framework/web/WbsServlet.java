package wbs.framework.web;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import wbs.framework.activitymanager.ActiveTask;
import wbs.framework.activitymanager.ActivityManager;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;

@Log4j
@SuppressWarnings ("serial")
public abstract
class WbsServlet
	extends HttpServlet {

	// singleton dependencies

	@SingletonDependency
	ActivityManager activityManager;

	// state

	protected
	ComponentManager componentManager;

	protected
	RequestContext requestContext;

	private
	ActiveTask startTask (
			@NonNull String methodName) {

		return activityManager.start (
			"web-request",
			stringFormat (
				"WbsServlet.%s () %s",
				methodName,
				requestContext.requestPath ()),
			this);

	}

	protected
	void doGet ()
		throws
			ServletException,
			IOException {

		@Cleanup
		ActiveTask activeTask =
			startTask (
				"doGet");

		try {

			WebFile file =
				processPath ();

			if (file != null) {

				file.doGet ();

			} else {

				handleNotFound ();

			}

			activeTask.success ();

		} catch (Throwable throwable) {

			activeTask.fail (
				throwable);

			handleException (
				throwable);

		}

	}

	protected
	void doPost ()
		throws
			ServletException,
			IOException {

		@Cleanup
		ActiveTask activeTask =
			startTask (
				"doPost");

		try {

			WebFile file =
				processPath ();

			if (file != null) {

				file.doPost ();

			} else {

				handleNotFound ();

			}

		} catch (Throwable exception) {

			handleException (
				exception);

		}

	}

	protected
	void doOptions ()
		throws
			ServletException,
			IOException {

		@Cleanup
		ActiveTask activeTask =
			startTask (
				"doOptions");

		try {

			WebFile file =
				processPath ();

			if (file != null) {

				file.doOptions ();

			} else {

				handleNotFound ();

			}

		} catch (Throwable exception) {

			handleException (
				exception);

		}

	}

	@Override
	protected final
	void doGet (
			HttpServletRequest request,
			HttpServletResponse response)
		throws
			ServletException,
			IOException {

		doGet ();

	}

	@Override
	protected
	final void doPost (
			HttpServletRequest request,
			HttpServletResponse response)
		throws
			ServletException,
			IOException {

		doPost ();

	}

	@Override
	protected
	final void doOptions (
			HttpServletRequest request,
			HttpServletResponse response)
		throws
			ServletException,
			IOException {

		doOptions ();

	}

	protected
	WebFile processPath ()
		throws ServletException {

		return null;

	}

	protected
	void handleException (
			Throwable exception)
		throws
			ServletException,
			IOException {

		if (exception instanceof ServletException) {

			throw (ServletException)
				exception;

		} else if (exception instanceof IOException) {

			throw (IOException)
				exception;

		} else if (exception instanceof MethodNotAllowedException) {

			handleMethodNotAllowed ();

		} else if (exception instanceof ForbiddenException) {

			handleForbidden (
				requestContext);

		} else if (exception instanceof ExternalRedirectException) {

			ExternalRedirectException redirectException =
				(ExternalRedirectException)
				exception;

			requestContext.sendRedirect (
				redirectException.getLocation ());

		} else if (exception instanceof RuntimeException) {

			throw (RuntimeException)
				exception;

		} else {

			throw new RuntimeException (
				exception);

		}

	}

	protected
	void handleNotFound ()
		throws
			ServletException,
			IOException {

		log.warn (
			stringFormat (
				"RCX not found %s %s",
				requestContext.servletPath (),
				requestContext.pathInfo ()));

		requestContext.sendError (
			HttpServletResponse.SC_NOT_FOUND,
			requestContext.requestUri ());

	}

	protected
	void handleMethodNotAllowed ()
		throws
			ServletException,
			IOException {

		log.warn (
			stringFormat (
				"RCX method not allowed %s %s",
				requestContext.servletPath (),
				requestContext.pathInfo ()));

		requestContext.sendError (
			HttpServletResponse.SC_METHOD_NOT_ALLOWED);

	}

	protected
	void handleForbidden (
			RequestContext requestContext)
		throws
			ServletException,
			IOException {

		log.warn (
			stringFormat (
				"RCX forbidden %s %s",
				requestContext.servletPath (),
				requestContext.pathInfo ()));

		requestContext.sendError (
			HttpServletResponse.SC_FORBIDDEN);

	}

	@Override
	public
	void init ()
		throws ServletException {

		ServletContext servletContext =
			getServletContext ();

		componentManager =
			(ComponentManager)
			servletContext.getAttribute (
				"wbs-application-context");

		if (componentManager == null) {

			throw new ServletException (
				"Application context not found");

		}

		requestContext =
			componentManager.getComponentRequired (
				"requestContext",
				RequestContext.class);

	}

}
