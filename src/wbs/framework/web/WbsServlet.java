package wbs.framework.web;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import wbs.framework.activitymanager.ActiveTask;
import wbs.framework.activitymanager.ActivityManager;
import wbs.framework.application.context.ApplicationContext;

import com.google.common.collect.ImmutableMap;

@Log4j
@SuppressWarnings ("serial")
public abstract
class WbsServlet
	extends HttpServlet {

	@Inject
	ActivityManager activityManager;

	protected
	ApplicationContext applicationContext;

	protected
	RequestContext requestContext;

	protected
	void doGet ()
		throws
			ServletException,
			IOException {

		@Cleanup
		ActiveTask activeTask =
			activityManager.start (
				"web request",
				this,
				ImmutableMap.<String,Object>builder ()
					.build ());

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

		applicationContext =
			(ApplicationContext)
			servletContext.getAttribute (
				"wbs-application-context");

		if (applicationContext == null) {

			throw new ServletException (
				"Application context not found");

		}

		requestContext =
			applicationContext.getBean (
				"requestContext",
				RequestContext.class);

	}

}
