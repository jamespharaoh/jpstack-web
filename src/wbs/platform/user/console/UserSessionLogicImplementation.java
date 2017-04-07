package wbs.platform.user.console;

import static wbs.utils.collection.MapUtils.mapItemForKey;
import static wbs.utils.etc.Misc.hashSha1Base64;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalMapRequired;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalValueNotEqualWithClass;
import static wbs.utils.string.StringUtils.joinWithFullStop;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;
import static wbs.utils.time.TimeUtils.earlierThan;
import static wbs.utils.time.TimeUtils.millisToInstant;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.text.console.TextConsoleHelper;
import wbs.platform.user.model.UserDataRec;
import wbs.platform.user.model.UserOnlineRec;
import wbs.platform.user.model.UserRec;
import wbs.platform.user.model.UserSessionRec;

import wbs.utils.random.RandomLogic;

@SingletonComponent ("userSessionLogic")
public
class UserSessionLogicImplementation
	implements UserSessionLogic {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RandomLogic randomLogic;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	TextConsoleHelper textHelper;

	@SingletonDependency
	UserDataConsoleHelper userDataHelper;

	@SingletonDependency
	UserConsoleHelper userHelper;

	@SingletonDependency
	UserOnlineConsoleHelper userOnlineHelper;

	@SingletonDependency
	UserSessionConsoleHelper userSessionHelper;

	@SingletonDependency
	WbsConfig wbsConfig;

	// state

	Instant lastReload =
		millisToInstant (0);

	Map <Long, String> onlineSessionIdsByUserId =
		new HashMap<> ();

	Map <String, Instant> activeSessions =
		new HashMap<> ();

	// implementation

	@Override
	public
	UserSessionRec userLogon (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull UserRec user,
			@NonNull Optional <String> userAgent,
			@NonNull Optional <String> consoleDeploymentCode) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"userLogon");

		Transaction transaction =
			database.currentTransaction ();

		// end any existing session

		userLogoff (
			taskLogger,
			user);

		// start the session log

		UserSessionRec session =
			userSessionHelper.insert (
				taskLogger,
				userSessionHelper.createInstance ()

			.setUser (
				user)

			.setStartTime (
				transaction.now ())

			.setUserAgent (
				textHelper.findOrCreate (
					taskLogger,
					userAgent.orNull ()))

		);

		// go online

		UserOnlineRec userOnline =
			userOnlineHelper.insert (
				taskLogger,
				userOnlineHelper.createInstance ()

			.setUser (
				user)

			.setSessionId (
				randomLogic.generateLowercase (
					20))

			.setTimestamp (
				transaction.now ())

			.setUserSession (
				session)

		);

		// create cookies

		requestContext.cookieSet (
			sessionIdCookieName,
			userOnline.getSessionId ());

		requestContext.cookieSet (
			userIdCookieName,
			integerToDecimalString (
				user.getId ()));

		// return

		return session;

	}

	@Override
	public
	void userLogoff (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull UserRec user) {

		Optional <UserOnlineRec> userOnlineOptional =
			userOnlineHelper.find (
				user.getId ());

		if (
			optionalIsNotPresent (
				userOnlineOptional)
		) {
			return;
		}

		UserOnlineRec userOnline =
			userOnlineOptional.get ();

		// end the session log

		UserSessionRec userSession =
			userOnline.getUserSession ();

		if (userSession != null) {

			userSession.setEndTime (
				userOnline.getTimestamp ());

		}

		// go offline

		userOnlineHelper.remove (
			userOnline);

	}

	@Override
	public
	Optional <UserSessionRec> userLogonTry (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String sliceCode,
			@NonNull String username,
			@NonNull String password,
			@NonNull Optional <String> userAgent,
			@NonNull Optional <String> consoleDeploymentCode) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"userLogonTry");

		// lookup the user

		Optional <UserRec> userOptional =
			userHelper.findByCode (
				GlobalId.root,
				sliceCode,
				username);

		if (
			optionalIsNotPresent (
				userOptional)
		) {
			return optionalAbsent ();
		}

		UserRec user =
			userOptional.get ();

		// check password

		if (
			! checkPassword (
				user,
				password)
		) {

			return optionalAbsent ();

		}

		// update user (bring online)

		UserSessionRec userSession =
			userLogon (
				taskLogger,
				user,
				userAgent,
				consoleDeploymentCode);

		// and return

		return optionalOf (
			userSession);

	}

	boolean checkPassword (
			@NonNull UserRec user,
			@NonNull String password) {

		// bypass for dev auto-login

		if (

			stringEqualSafe (
				password,
				"**********")

			&& wbsConfig.testUsers ().contains (
				joinWithFullStop (
					user.getSlice ().getCode (),
					user.getUsername ()))

		) {

			return true;

		}

		// always fail if user has no password

		if (user.getPassword () == null) {

			return false;

		}

		// fail if password hash doesn't match

		if (
			stringNotEqualSafe (
				user.getPassword (),
				hashSha1Base64 (
					password))
		) {

			return false;

		}

		// all correct

		return true;

	}

	@Override
	public synchronized
	boolean userSessionVerify (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"checkUser");

		Instant now =
			Instant.now ();

		// check the cookies are present

		if (

			optionalIsNotPresent (
				requestContext.cookie (
					sessionIdCookieName))

			|| optionalIsNotPresent (
				requestContext.cookie (
					userIdCookieName))

		) {

			taskLogger.debugFormat (
				"Cookies not found");

			return false;

		}

		String sessionId =
			requestContext.cookieRequired (
				sessionIdCookieName);

		Long userId =
			parseIntegerRequired (
				requestContext.cookieRequired (
					userIdCookieName));

		// reload if overdue or not done yet, always for root path

		boolean reloaded = false;

		if (

			earlierThan (
				lastReload.plus (
					reloadTime),
				now)

			|| stringEqualSafe (
				requestContext.servletPath (),
				"/")

		) {

			taskLogger.debugFormat (
				"Reloading");

			reload (
				taskLogger);

			lastReload =
				now;

			reloaded = true;

		}

		// check his session is valid, reload if it doesn't look right still

		if (
			optionalValueNotEqualWithClass (
				String.class,
				mapItemForKey (
					onlineSessionIdsByUserId,
					userId),
				sessionId)
		) {

			taskLogger.debugFormat (
				"Session not found in cache (1)");

			if (reloaded) {
				return false;
			}

			taskLogger.debugFormat (
				"Reloading");

			reload (
				taskLogger);

			lastReload =
				now;

			if (
				optionalValueNotEqualWithClass (
					String.class,
					mapItemForKey (
						onlineSessionIdsByUserId,
						userId),
					sessionId)
			) {

				taskLogger.debugFormat (
					"Session not found in cache (2)");

				return false;

			}

		}

		// update his timestamp next time round

		taskLogger.debugFormat (
			"Session ok");

		activeSessions.put (
			sessionId,
			now);

		return true;

	}

	// private implementation

	private synchronized
	void reload (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"reload");

		try (

			Transaction transaction =
				database.beginReadWrite (
					"UserSessionLogicImplementation.reload ()",
					this);

		) {

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

					userLogoff (
						taskLogger,
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
				new HashMap<> ();

		}

	}

	@Override
	public
	Optional <byte[]> userData (
			@NonNull UserRec user,
			@NonNull String code) {

		return optionalMapRequired (
			userDataHelper.findByCode (
				user,
				code),
			UserDataRec::getData);

	}

	@Override
	public
	void userDataStore (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull UserRec user,
			@NonNull String code,
			@NonNull byte[] value) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"userDataStore");

		Transaction transaction =
			database.currentTransaction ();

		Optional <UserDataRec> existingUserDataOptional =
			userDataHelper.findByCode (
				user,
				code);

		if (
			optionalIsPresent (
				existingUserDataOptional)
		) {

			optionalGetRequired (
				existingUserDataOptional)

				.setCreatedTime (
					transaction.now ())

				.setExpiryTime (
					transaction.now ().plus (
						Duration.standardDays (
							1l)))

				.setData (
					value)

			;

		} else {

			userDataHelper.insert (
				taskLogger,
				userDataHelper.createInstance ()

				.setUser (
					user)

				.setCode (
					code)

				.setCreatedTime (
					transaction.now ())

				.setExpiryTime (
					transaction.now ().plus (
						Duration.standardDays (
							1l)))

				.setData (
					value)

			);

		}

	}

	@Override
	public
	void userDataRemove (
			@NonNull UserRec user,
			@NonNull String code) {

		Optional <UserDataRec> userDataOptional =
			userDataHelper.findByCode (
				user,
				code);

		if (
			optionalIsPresent (
				userDataOptional)
		) {

			userDataHelper.remove (
				optionalGetRequired (
					userDataOptional));

		}

	}

	// constants

	public final static
	String sessionIdCookieName =
		"wbs-session-id";

	public final static
	String userIdCookieName =
		"wbs-user-id";

	public final static
	Duration reloadTime =
		Duration.standardSeconds (
			1l);

	public final static
	Duration logoffTime =
		Duration.standardMinutes (
			5l);

}
