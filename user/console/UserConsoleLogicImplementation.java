package wbs.platform.user.console;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalMapOptional;
import static wbs.utils.etc.OptionalUtils.optionalMapRequired;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.ReadableDuration;
import org.joda.time.ReadableInstant;

import wbs.console.misc.ConsoleUserHelper;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

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

	@ClassSingletonDependency
	LogContext logContext;

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
	Optional <UserRec> user (
			@NonNull Transaction parentTransaction) {

		return optionalMapOptional (
			userId (),
			userId ->
				userHelper.find (
					parentTransaction,
					userId));

	}

	@Override
	public
	UserRec userRequired (
			@NonNull Transaction parentTransaction) {

		return userHelper.findRequired (
			parentTransaction,
			userIdRequired ());

	}

	@Override
	public
	Optional <SliceRec> slice (
			@NonNull Transaction parentTransaction) {

		return optionalMapRequired (
			user (
				parentTransaction),
			UserRec::getSlice);

	}

	@Override
	public
	SliceRec sliceRequired (
			@NonNull Transaction parentTransaction) {

		return userRequired (
			parentTransaction
		).getSlice ();

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
	Optional <Long> sliceId (
			@NonNull Transaction parentTransaction) {

		return optionalMapRequired (
			slice (
				parentTransaction),
			SliceRec::getId);

	}

	@Override
	public
	Long sliceIdRequired (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"sliceIdRequired");

		) {

			UserRec user =
				userRequired (
					transaction);

			return user.getSlice ().getId ();

		}

	}

	@Override
	public
	DateTimeZone timezone (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"timezone");

		) {

			DateTimeZone timezone =
				timeFormatter.timezone (
					ifNull (
						() ->
							userRequired (
								parentTransaction
							).getDefaultTimezone (),
						() ->
							sliceRequired (
								parentTransaction
							).getDefaultTimezone (),
						() -> wbsConfig.defaultTimezone ()));

			return timezone;

		}

	}

	@Override
	public
	Long hourOffset (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"hourOffset");

		) {

			SliceRec slice =
				sliceRequired (
					transaction);

			return slice.getDefaultHourOffset ();

		}

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

	// console user helper

	@Override
	public
	String dateStringLong (
			@NonNull Transaction parentTransaction,
			@NonNull ReadableInstant timestamp) {

		return timeFormatter.dateStringLong (
			timezone (
				parentTransaction),
			timestamp);

	}

	@Override
	public
	String dateStringShort (
			@NonNull Transaction parentTransaction,
			@NonNull ReadableInstant timestamp) {

		return timeFormatter.dateStringShort (
			timezone (
				parentTransaction),
			timestamp);

	}

	@Override
	public
	String timeString (
			@NonNull Transaction parentTransaction,
			@NonNull ReadableInstant timestamp) {

		return timeFormatter.timeString (
			timezone (
				parentTransaction),
			timestamp);

	}

	@Override
	public
	String timestampWithTimezoneString (
			@NonNull Transaction parentTransaction,
			@NonNull ReadableInstant timestamp) {

		return timeFormatter.timestampTimezoneString (
			timezone (
				parentTransaction),
			timestamp);

	}

	@Override
	public
	String timestampWithoutTimezoneString (
			@NonNull Transaction parentTransaction,
			@NonNull ReadableInstant timestamp) {

		return timeFormatter.timestampString (
			timezone (
				parentTransaction),
			timestamp);

	}

	@Override
	public
	String prettyDuration (
			@NonNull Transaction parentTransaction,
			@NonNull ReadableDuration duration) {

		return timeFormatter.prettyDuration (
			duration);

	}

	@Override
	public
	Instant timestampStringToInstant (
			@NonNull Transaction parentTransaction,
			@NonNull String string) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"timestampStringToInstant");

		) {

			return timeFormatter.timestampStringToInstant (
				timezone (
					transaction),
				string);

		}

	}

	@Override
	public
	String timezoneString (
			@NonNull Transaction parentTransaction,
			@NonNull ReadableInstant timestamp) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"timezoneString");

		) {

			return timeFormatter.timestampString (
				timezone (
					transaction),
				timestamp);

		}

	}

	// constants

	final static
	String loggedInUserIdSessionKey =
		"logged-in-user-id";

}
