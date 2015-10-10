package wbs.platform.exception.logic;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import lombok.NonNull;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.exception.ExceptionLogicImplementation;
import wbs.framework.record.GlobalId;
import wbs.platform.exception.model.ExceptionLogObjectHelper;
import wbs.platform.exception.model.ExceptionLogRec;
import wbs.platform.exception.model.ExceptionLogTypeObjectHelper;
import wbs.platform.exception.model.ExceptionLogTypeRec;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

import com.google.common.base.Optional;

@SingletonComponent ("exceptionLogLogic")
public
class ExceptionLogLogicImplementation
	implements ExceptionLogLogic {

	// dependencies

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
			@NonNull Boolean fatal) {

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
				new ExceptionLogRec ()

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
				fatal)

		);

		return exceptionLog;

	}

}
