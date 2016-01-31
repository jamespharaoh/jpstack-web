package wbs.platform.exception.logic;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Map;

import javax.inject.Inject;

import lombok.NonNull;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogicImplementation;
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
			@NonNull Optional<Integer> userId,
			@NonNull GenericExceptionResolution resolution) {

		Transaction transaction =
			database.currentTransaction ();

		// lookup type

		ExceptionLogTypeRec exceptionLogType =
			exceptionLogTypeHelper.findByCode (
				GlobalId.root,
				typeCode);

		if (exceptionLogType == null) {

			throw new RuntimeException (
				stringFormat (
					"Unknown exception type: %s",
					typeCode));

		}

		// lookup user

		UserRec user =
			userId.isPresent ()
				? userHelper.find (userId.get ())
				: null;

		// create exception log

		ExceptionLogRec exceptionLog =
			exceptionLogHelper.insert (
				exceptionLogHelper.createInstance ()

			.setTimestamp (
				instantToDate (
					transaction.now ()))

			.setType (
				exceptionLogType)

			.setSource (
				source)

			.setSummary (
				ExceptionLogicImplementation.substituteNuls (
					summary))

			.setUser (
				user)

			.setDump (
				ExceptionLogicImplementation.substituteNuls (
					dump))

			.setFatal (
				equal (
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