package wbs.platform.user.console;

import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.requiredValue;

import javax.inject.Inject;

import org.joda.time.DateTimeZone;

import com.google.common.base.Optional;

import lombok.NonNull;
import wbs.console.misc.ConsoleUserHelper;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.utils.TimeFormatter;
import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.user.model.UserRec;

@SingletonComponent ("userConsoleLogic")
public
class UserConsoleLogicImplementation
	implements
		ConsoleUserHelper,
		UserConsoleLogic {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	TimeFormatter timeFormatter;

	@Inject
	UserConsoleHelper userHelper;

	@Inject
	WbsConfig wbsConfig;

	// implementation

	@Override
	public
	TimeFormatter timeFormatter () {

		return timeFormatter;

	}

	@Override
	public
	Optional<UserRec> user () {

		return loggedIn ()
			? Optional.of (
				userRequired ())
			: Optional.absent ();

	}

	@Override
	public
	UserRec userRequired () {

		return userHelper.findRequired (
			userIdRequired ());

	}

	@Override
	public
	Optional<SliceRec> slice () {

		return loggedIn ()
			? Optional.of (
				userRequired ().getSlice ())
			: Optional.absent ();

	}

	@Override
	public
	SliceRec sliceRequired () {

		return userRequired ().getSlice ();

	}

	@Override
	public
	Optional<Long> userId () {

		return Optional.fromNullable (
			(Long)
			requestContext.session (
				LOGGED_IN_USER_ID_SESSION_KEY));

	}

	@Override
	public
	Long userIdRequired () {

		return requiredValue (
			(Long)
			requestContext.session (
				LOGGED_IN_USER_ID_SESSION_KEY));

	}

	@Override
	public
	Optional<Long> sliceId () {

		return loggedIn ()
			? Optional.of (
				userRequired ().getSlice ().getId ())
			: Optional.absent ();

	}

	@Override
	public
	Long sliceIdRequired () {

		return userRequired ().getSlice ().getId ();

	}

	@Override
	public
	boolean loggedIn () {

		return isNotNull (
			requestContext.session (
				LOGGED_IN_USER_ID_SESSION_KEY));

	}

	@Override
	public
	boolean notLoggedIn () {

		return isNull (
			requestContext.session (
				LOGGED_IN_USER_ID_SESSION_KEY));

	}

	@Override
	public
	DateTimeZone timezone () {

		DateTimeZone timezone =
			timeFormatter.timezone (
				ifNull (
					userRequired ().getDefaultTimezone (),
					sliceRequired ().getDefaultTimezone (),
					wbsConfig.defaultTimezone ()));

		return timezone;

	}

	@Override
	public
	Long hourOffset () {

		return sliceRequired ().getDefaultHourOffset ();

	}

	@Override
	public
	Optional<Long> loggedInUserId () {
		return userId ();
	}

	@Override
	public
	Long loggedInUserIdRequired () {
		return userIdRequired ();
	}

	@Override
	public
	void login (
			@NonNull Long userId) {

		requestContext.session (
			LOGGED_IN_USER_ID_SESSION_KEY,
			userId);

	}

	@Override
	public
	void logout () {

		requestContext.session (
			LOGGED_IN_USER_ID_SESSION_KEY,
			null);

	}

	// data

	final static
	String LOGGED_IN_USER_ID_SESSION_KEY =
		"logged-in-user-id";

}
