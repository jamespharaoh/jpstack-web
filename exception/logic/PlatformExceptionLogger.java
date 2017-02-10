package wbs.platform.exception.logic;

import static wbs.utils.string.StringUtils.stringFormat;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.ExceptionUtils;
import wbs.framework.exception.GenericExceptionResolution;

import wbs.platform.exception.model.ExceptionLogRec;

@Log4j
@SingletonComponent ("platformExceptionLogger")
public
class PlatformExceptionLogger
	implements ExceptionLogger {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionLogLogic exceptionLogLogic;

	@SingletonDependency
	ExceptionUtils exceptionLogic;

	// implementation

	@Override
	public
	ExceptionLogRec logSimple (
			@NonNull String typeCode,
			@NonNull String source,
			@NonNull String summary,
			@NonNull String dump,
			@NonNull Optional <Long> userId,
			@NonNull GenericExceptionResolution resolution) {

		return logExceptionWrapped (
			typeCode,
			source,
			userId,
			new Provider<ExceptionLogRec> () {

			@Override
			public
			ExceptionLogRec get () {

				return realLogException (
					typeCode,
					source,
					summary,
					dump,
					userId,
					resolution);

			}

		});

	}

	@Override
	public
	ExceptionLogRec logThrowable (
			@NonNull String typeCode,
			@NonNull String source,
			@NonNull Throwable throwable,
			@NonNull Optional <Long> userId,
			@NonNull GenericExceptionResolution resolution) {

		return logExceptionWrapped (
			typeCode,
			source,
			userId,
			() -> realLogException (
				typeCode,
				source,
				exceptionLogic.throwableSummary (
					throwable),
				exceptionLogic.throwableDump (
					throwable),
				userId,
				resolution));

	}

	@Override
	public
	ExceptionLogRec logThrowableWithSummary (
			@NonNull String typeCode,
			@NonNull String source,
			@NonNull String summary,
			@NonNull Throwable throwable,
			@NonNull Optional <Long> userId,
			@NonNull GenericExceptionResolution resolution) {

		return logExceptionWrapped (
			typeCode,
			source,
			userId,
			() -> realLogException (
				typeCode,
				source,
				stringFormat (
					"%s\n%s",
					summary,
					exceptionLogic.throwableSummary (
						throwable)),
				exceptionLogic.throwableDump (
					throwable),
				userId,
				resolution));

	}

	ExceptionLogRec logExceptionWrapped (
			@NonNull String typeCode,
			@NonNull String source,
			@NonNull Optional <Long> userId,
			@NonNull Provider <ExceptionLogRec> target) {

		try {

			return target.get ();

		} catch (Exception furtherException) {

			log.fatal (
				"Error logging exception",
				furtherException);

			String furtherSummary =
				stringFormat (
					"Threw %s while logging exception from %s",
					furtherException.getClass ().getSimpleName (),
					source);

			try {

				realLogException (
					typeCode,
					"exception log",
					stringFormat (
						"%s\n%s",
						furtherSummary,
						exceptionLogic.throwableSummary (
							furtherException)),
					exceptionLogic.throwableDump (
						furtherException),
					Optional.absent (),
					GenericExceptionResolution.fatalError);

			} catch (Exception yetAnotherException) {

				log.fatal (
					"Error logging error logging exception",
					yetAnotherException);

			}

			return null;

		}

	}

	private
	ExceptionLogRec realLogException (
			@NonNull String typeCode,
			@NonNull String source,
			@NonNull String summary,
			@NonNull String dump,
			@NonNull Optional <Long> userId,
			@NonNull GenericExceptionResolution resolution) {

		try (

			Transaction transaction =
				database.beginReadWrite (
					"PlatformExceptionLogger.realLogException (...)",
					this);

		) {

			ExceptionLogRec exceptionLog =
				exceptionLogLogic.logException (
					typeCode,
					source,
					summary,
					dump,
					userId,
					resolution);

			transaction.commit ();

			return exceptionLog;

		}

	}

}
