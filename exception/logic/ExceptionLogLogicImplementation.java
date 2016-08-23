package wbs.platform.exception.logic;

import static wbs.framework.utils.etc.EnumUtils.enumEqualSafe;

import java.util.Map;

import javax.inject.Inject;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionUtilsImplementation;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.record.GlobalId;
import wbs.platform.exception.model.ConcreteExceptionResolution;
import wbs.platform.exception.model.ExceptionLogObjectHelper;
import wbs.platform.exception.model.ExceptionLogRec;
import wbs.platform.exception.model.ExceptionLogTypeObjectHelper;
import wbs.platform.exception.model.ExceptionLogTypeRec;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

@SingletonComponent ("exceptionLogLogic")
public
class ExceptionLogLogicImplementation
	implements ExceptionLogLogic {

	// dependencies

	@Inject
	Database database;

	@Inject
	ExceptionLogObjectHelper exceptionLogHelper;

	@Inject
	ExceptionLogTypeObjectHelper exceptionLogTypeHelper;

	@Inject
	UserObjectHelper userHelper;

	// implementation

	@Override
	public
	ExceptionLogRec logException (
			@NonNull String typeCode,
			@NonNull String source,
			@NonNull String summary,
			@NonNull String dump,
			@NonNull Optional<Long> userId,
			@NonNull GenericExceptionResolution resolution) {

		Transaction transaction =
			database.currentTransaction ();

		// lookup type

		ExceptionLogTypeRec exceptionLogType =
			exceptionLogTypeHelper.findByCodeRequired (
				GlobalId.root,
				typeCode);

		// lookup user

		UserRec user =
			userId.isPresent ()
				? userHelper.findRequired (
					userId.get ())
				: null;

		// create exception log

		ExceptionLogRec exceptionLog =
			exceptionLogHelper.insert (
				exceptionLogHelper.createInstance ()

			.setTimestamp (
				transaction.now ())

			.setType (
				exceptionLogType)

			.setSource (
				source)

			.setSummary (
				ExceptionUtilsImplementation.substituteNuls (
					summary))

			.setUser (
				user)

			.setDump (
				ExceptionUtilsImplementation.substituteNuls (
					dump))

			.setFatal (
				enumEqualSafe (
					resolution,
					GenericExceptionResolution.fatalError))

			.setResolution (
				conreteMap.get (
					resolution))

		);

		return exceptionLog;

	}

	// data

	final static
	Map<GenericExceptionResolution,ConcreteExceptionResolution> conreteMap =
		ImmutableMap.<GenericExceptionResolution,ConcreteExceptionResolution>builder ()

		.put (
			GenericExceptionResolution.tryAgainNow,
			ConcreteExceptionResolution.tryAgainNow)

		.put (
			GenericExceptionResolution.tryAgainLater,
			ConcreteExceptionResolution.tryAgainLater)

		.put (
			GenericExceptionResolution.ignoreWithUserWarning,
			ConcreteExceptionResolution.ignoreWithUserWarning)

		.put (
			GenericExceptionResolution.ignoreWithThirdPartyWarning,
			ConcreteExceptionResolution.ignoreWithThirdPartyWarning)

		.put (
			GenericExceptionResolution.ignoreWithLoggedWarning,
			ConcreteExceptionResolution.ignoreWithLoggedWarning)

		.put (
			GenericExceptionResolution.ignoreWithNoWarning,
			ConcreteExceptionResolution.ignoreWithNoWarning)

		.put (
			GenericExceptionResolution.fatalError,
			ConcreteExceptionResolution.fatalError)

		.build ();

}