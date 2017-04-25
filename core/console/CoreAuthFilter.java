package wbs.platform.core.console;

import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.string.StringUtils.stringInSafe;

import java.io.IOException;

import javax.inject.Provider;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import lombok.NonNull;

import wbs.console.misc.JqueryScriptRef;
import wbs.console.module.ConsoleManager;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.console.UserSessionLogic;

import wbs.web.responder.Responder;

@SingletonComponent ("coreAuthFilter")
public
class CoreAuthFilter
	implements Filter {

	// singleton dependencies

	@WeakSingletonDependency
	ConsoleManager consoleManager;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserPrivChecker userPrivChecker;

	@SingletonDependency
	UserSessionLogic userSessionLogic;

	// constants

	@Override
	public
	void doFilter (
			@NonNull ServletRequest request,
			@NonNull ServletResponse response,
			@NonNull FilterChain chain)
		throws
			ServletException,
			IOException {

		TaskLogger taskLogger =
			logContext.createTaskLogger (
				"doFilter",
				true);

		String path =
			requestContext.servletPath ();

		// check the user is ok

		taskLogger.debugFormat (
			"Verify user session");

		boolean userOk =
			userSessionLogic.userSessionVerify (
				taskLogger,
				requestContext);

		if (userOk) {

			taskLogger.debugFormat (
				"User session verified");

			// and show the page

			chain.doFilter (
				request,
				response);

		} else {

			taskLogger.debugFormat (
				"User session not verified");

			// user not ok, either....

			if (path.equals ("/")) {

				// root path, either show logon page or process logon request

				if (requestContext.post ()) {

					chain.doFilter (
						request,
						response);

				} else {

					Provider <Responder> logonResponder =
						consoleManager.responder (
							"coreLogonResponder",
							true);

					logonResponder
						.get ()
						.execute (
							taskLogger);

				}

			} else if (

				stringInSafe (
					path,
					"/style/basic.css",
					"/favicon.ico",
					"/status.update",
					"/js/login.js",
					JqueryScriptRef.path)

			) {

				// these paths are available before login

				chain.doFilter (
					request,
					response);

			} else {

				// unauthorised access, redirect to the logon page

				requestContext.sendRedirect (
					requestContext.resolveApplicationUrl (
						"/"));

			}

		}

	}

	@Override
	public
	void destroy () {

		doNothing ();

	}

	@Override
	public
	void init (
			@NonNull FilterConfig filterConfig) {

		doNothing ();

	}

}
