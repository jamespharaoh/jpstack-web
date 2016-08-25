package wbs.platform.core.console;

import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.framework.utils.etc.OptionalUtils.optionalValueNotEqualSafe;
import static wbs.framework.utils.etc.StringUtils.stringEqualSafe;
import static wbs.framework.utils.etc.StringUtils.stringInSafe;
import static wbs.framework.utils.etc.TimeUtils.earlierThan;
import static wbs.framework.utils.etc.TimeUtils.millisToInstant;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.joda.time.Instant;

import lombok.Cleanup;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.module.ConsoleManager;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.logic.UserLogic;
import wbs.platform.user.model.UserOnlineObjectHelper;
import wbs.platform.user.model.UserOnlineRec;
import wbs.platform.user.model.UserRec;

@SingletonComponent ("coreAuthFilter")
public
class CoreAuthFilter
	implements Filter {

	@Inject
	Provider<ConsoleManager> consoleManagerProvider;

	@Inject
	Database database;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	UserConsoleLogic userConsoleLogic;

	@Inject
	UserLogic userLogic;

	@Inject
	UserOnlineObjectHelper userOnlineHelper;

	@Inject
	UserPrivChecker userPrivChecker;

	final static
	int reloadTime = 10 * 1000;

	final static
	int logoffTime = 5 * 60 * 1000;

	Instant lastReload =
		millisToInstant (0);

	Map <Long, String> onlineSessionIdsByUserId;

	Map <String, Instant> activeSessions =
		new HashMap<> ();

	private synchronized
	void reload () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"CoreAuthFilter.reload ()",
				this);

		onlineSessionIdsByUserId =
			new HashMap <> ();

		for (
			UserOnlineRec online
				: userOnlineHelper.findAll ()
		) {

			UserRec user =
				online.getUser ();

			// update his timestamp if appropriate

			if (
				activeSessions.containsKey (
					online.getSessionId ())
			)

				online

					.setTimestamp (
						activeSessions.get (
							online.getSessionId ()));

			// check if he has been disabled or timed out

			if (

				! user.getActive ()

				|| earlierThan (
					online.getTimestamp ().plus (
						logoffTime),
					transaction.now ())

			) {

				userLogic.userLogoff (
					user);

				continue;

			}

			// ok put him in the ok list

			onlineSessionIdsByUserId.put (
				user.getId (),
				online.getSessionId ());

		}

		transaction.commit ();

		activeSessions =
			new HashMap<String,Instant> ();

	}

	/**
	 * Performs the main authorisation. Calls reload () to refresh caches if
	 * necessary then checks the user's session against them. Also adds us to an
	 * "active" list which updates our user record with a timestamp
	 * automatically when the caches are updated.
	 */
	private synchronized
	boolean checkUser () {

		Instant now =
			Instant.now ();

		// check there is a user id

		if (userConsoleLogic.notLoggedIn ()) {
			return false;
		}

		// reload if overdue or not done yet, always for root path

		boolean reloaded = false;

		if (

			isNull (
				onlineSessionIdsByUserId)

			|| earlierThan (
				lastReload.plus (
					reloadTime),
				now)

			|| stringEqualSafe (
				requestContext.servletPath (),
				"/")

		) {

			reload ();

			lastReload =
				now;

			reloaded = true;

		}

		// check his session is valid, reload if it doesn't look right still

		if (
			optionalValueNotEqualSafe (
				optionalFromNullable (
					onlineSessionIdsByUserId.get (
						userConsoleLogic.userIdRequired ())),
				requestContext.sessionId ())
		) {

			if (reloaded)
				return false;

			reload ();

			lastReload =
				now;

			if (
				optionalValueNotEqualSafe (
					optionalFromNullable (
						onlineSessionIdsByUserId.get (
							userConsoleLogic.userIdRequired ())),
					requestContext.sessionId ())
			) {
				return false;
			}

		}

		// update his timestamp next time round

		activeSessions.put (
			requestContext.sessionId (),
			now);

		return true;

	}

	@Override
	public
	void doFilter (
			ServletRequest request,
			ServletResponse response,
			FilterChain chain)
		throws
			ServletException,
			IOException {

		String path =
			requestContext.servletPath ();

		// check the user is ok

		boolean userOk =
			checkUser ();

		if (userOk) {

			// and show the page

			chain.doFilter (
				request,
				response);

		} else {

			// user not ok, either....

			if (path.equals ("/")) {

				// root path, either show logon page or process logon request

				if (requestContext.post ()) {

					chain.doFilter (
						request,
						response);

				} else {

					Provider <Responder> logonResponder =
						consoleManagerProvider.get ().responder (
							"coreLogonResponder",
							true);

					logonResponder
						.get ()
						.execute ();

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
	}

	@Override
	public
	void init (
			FilterConfig filterConfig)
		throws ServletException {

	}

}
