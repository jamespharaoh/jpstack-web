package wbs.platform.user.console;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalMapOptional;
import static wbs.utils.etc.OptionalUtils.optionalMapRequired;

import com.google.common.base.Optional;

import org.joda.time.DateTimeZone;

import wbs.console.misc.ConsoleUserHelper;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;

import wbs.platform.feature.console.FeatureConsoleHelper;
import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.user.model.UserRec;

import wbs.utils.etc.NumberUtils;
import wbs.utils.random.RandomLogic;
import wbs.utils.time.TimeFormatter;

@SingletonComponent ("userConsoleLogic")
public
class UserConsoleLogicImplementation
	implements
		ConsoleUserHelper,
		UserConsoleLogic {

	// singleton dependencies

	@SingletonDependency
	FeatureConsoleHelper featureHelper;

	@SingletonDependency
	RandomLogic randomLogic;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	TimeFormatter timeFormatter;

	@SingletonDependency
	UserConsoleHelper userHelper;

	@SingletonDependency
	WbsConfig wbsConfig;

	// implementation

	@Override
	public
	TimeFormatter timeFormatter () {

		return timeFormatter;

	}

	@Override
	public
	Optional <UserRec> user () {

		return optionalMapOptional (
			userId (),
			userHelper::find);

	}

	@Override
	public
	UserRec userRequired () {

		return userHelper.findRequired (
			userIdRequired ());

	}

	@Override
	public
	Optional <SliceRec> slice () {

		return optionalMapRequired (
			user (),
			UserRec::getSlice);

	}

	@Override
	public
	SliceRec sliceRequired () {

		return userRequired ().getSlice ();

	}

	@Override
	public
	Optional <Long> userId () {

		return optionalMapOptional (
			requestContext.cookie (
				"wbs-user-id"),
			NumberUtils::parseInteger);

	}

	@Override
	public
	Long userIdRequired () {

		return optionalGetRequired (
			userId ());

	}

	@Override
	public
	Optional <Long> sliceId () {

		return optionalMapRequired (
			slice (),
			SliceRec::getId);

	}

	@Override
	public
	Long sliceIdRequired () {

		return userRequired ().getSlice ().getId ();

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

	// data

	final static
	String LOGGED_IN_USER_ID_SESSION_KEY =
		"logged-in-user-id";

}
