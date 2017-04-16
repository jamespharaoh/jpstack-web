package wbs.web.servlet;

import static wbs.utils.etc.LogicUtils.parseBooleanYesNoRequired;
import static wbs.utils.etc.NumberUtils.fromJavaInteger;
import static wbs.utils.etc.OptionalUtils.optionalOr;
import static wbs.utils.string.StringUtils.stringFormat;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.NonNull;

import wbs.framework.activitymanager.ActiveTask;
import wbs.framework.activitymanager.ActivityManager;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.logging.Log4jLogContext;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.web.context.RequestContext;
import wbs.web.exceptions.ExternalRedirectException;
import wbs.web.exceptions.HttpForbiddenException;
import wbs.web.exceptions.HttpMethodNotAllowedException;
import wbs.web.file.WebFile;

@SuppressWarnings ("serial")
public abstract
class WbsServlet
	extends HttpServlet {

	private final static
	LogContext logContext =
		Log4jLogContext.forClass (
			WbsServlet.class);

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

		TaskLogger taskLogger =
			logContext.createTaskLogger (
				"doGet",
				parseBooleanYesNoRequired (
					optionalOr (
						requestContext.cookie (
							"wbs-debug"),
						"no")));

		try (

			ActiveTask activeTask =
				startTask (
					"doGet");

		) {

			try {

				WebFile file =
					processPath (
						taskLogger);

				if (file != null) {

					file.doGet (
						taskLogger);

				} else {

					handleNotFound (
						taskLogger);

				}

				activeTask.success ();

			} catch (Throwable throwable) {

				activeTask.fail (
					throwable);

				handleException (
					taskLogger,
					throwable);

			}

		}

	}

	protected
	void doPost ()
		throws
			ServletException,
			IOException {

		TaskLogger taskLogger =
			logContext.createTaskLogger (
				"doPost",
				parseBooleanYesNoRequired (
					optionalOr (
						requestContext.cookie (
							"wbs-debug"),
						"no")));

		try (

			ActiveTask activeTask =
				startTask (
					"doPost");

		) {

			WebFile file =
				processPath (
					taskLogger);

			if (file != null) {

				file.doPost (
					taskLogger);

			} else {

				handleNotFound (
					taskLogger);

			}

		} catch (Throwable exception) {

			handleException (
				taskLogger,
				exception);

		}

	}

	protected
	void doOptions ()
		throws
			ServletException,
			IOException {

		TaskLogger taskLogger =
			logContext.createTaskLogger (
				"doOptions",
				parseBooleanYesNoRequired (
					optionalOr (
						requestContext.cookie (
							"wbs-debug"),
						"no")));

		try (

			ActiveTask activeTask =
				startTask (
					"doOptions");

		) {

			try {

				WebFile file =
					processPath (
						taskLogger);

				if (file != null) {

					file.doOptions (
						taskLogger);

				} else {

					handleNotFound (
						taskLogger);

				}

			} catch (Throwable exception) {

				handleException (
					taskLogger,
					exception);

			}

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
	WebFile processPath (
			@NonNull TaskLogger taskLogger)
		throws ServletException {

		return null;

	}

	protected
	void handleException (
			@NonNull TaskLogger taskLogger,
			@NonNull Throwable exception)
		throws
			ServletException,
			IOException {

		if (exception instanceof ServletException) {

			throw (ServletException)
				exception;

		} else if (exception instanceof IOException) {

			throw (IOException)
				exception;

		} else if (exception instanceof HttpMethodNotAllowedException) {

			handleMethodNotAllowed (
				taskLogger);

		} else if (exception instanceof HttpForbiddenException) {

			handleForbidden (
				taskLogger,
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
	void handleNotFound (
			@NonNull TaskLogger taskLogger)
		throws
			ServletException,
			IOException {

		taskLogger.warningFormat (
			"RCX not found %s",
			requestContext.requestUri ());

		requestContext.sendError (
			fromJavaInteger (
				HttpServletResponse.SC_NOT_FOUND),
			requestContext.requestUri ());

	}

	protected
	void handleMethodNotAllowed (
			@NonNull TaskLogger taskLogger)
		throws
			ServletException,
			IOException {

		taskLogger.warningFormat (
			"method not allowed %s",
			requestContext.requestUri ());

		requestContext.sendError (
			fromJavaInteger (
				HttpServletResponse.SC_METHOD_NOT_ALLOWED));

	}

	protected
	void handleForbidden (
			@NonNull TaskLogger taskLogger,
			@NonNull RequestContext requestContext)
		throws
			ServletException,
			IOException {

		taskLogger.warningFormat (
			"RCX forbidden %s",
			requestContext.requestUri ());

		requestContext.sendError (
			fromJavaInteger (
				HttpServletResponse.SC_FORBIDDEN));

	}

	@Override
	public
	void init ()
		throws ServletException {

		TaskLogger taskLogger =
			logContext.createTaskLogger (
				"init");

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
				taskLogger,
				"requestContext",
				RequestContext.class);

		taskLogger.makeException ();

	}

}
