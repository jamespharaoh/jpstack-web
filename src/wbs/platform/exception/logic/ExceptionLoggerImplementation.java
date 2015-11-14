package wbs.platform.exception.logic;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.ExceptionLogic;
import wbs.platform.exception.model.ExceptionLogRec;

@SingletonComponent ("exceptionLogger")
@Log4j
public
class ExceptionLoggerImplementation
	implements ExceptionLogger {

	// dependencies

	@Inject
	Database database;

	@Inject
	ExceptionLogLogic exceptionLogLogic;

	@Inject
	ExceptionLogic exceptionLogic;

	// implementation

	@Override
	public
	ExceptionLogRec logSimple (
			final @NonNull String typeCode,
			final @NonNull String source,
			final @NonNull String summary,
			final @NonNull String dump,
			final @NonNull Optional<Integer> userId,
			final @NonNull Resolution resolution) {

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
			final @NonNull String typeCode,
			final @NonNull String source,
			final @NonNull Throwable throwable,
			final @NonNull Optional<Integer> userId,
			final @NonNull Resolution resolution) {

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
					exceptionLogic.throwableSummary (throwable),
					exceptionLogic.throwableDump (throwable),
					userId,
					resolution);

			}

		});

	}

	@Override
	public
	ExceptionLogRec logThrowableWithSummary (
			final @NonNull String typeCode,
			final @NonNull String source,
			final @NonNull String summary,
			final @NonNull Throwable throwable,
			final @NonNull Optional<Integer> userId,
			final @NonNull Resolution resolution) {

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
					stringFormat (
						"%s\n%s",
						summary,
						exceptionLogic.throwableSummary (
							throwable)),
					exceptionLogic.throwableDump (
						throwable),
					userId,
					resolution);

			}

		});

	}

	ExceptionLogRec logExceptionWrapped (
			@NonNull String typeCode,
			@NonNull String source,
			@NonNull Optional<Integer> userId,
			@NonNull Provider<ExceptionLogRec> target) {

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
					Optional.<Integer>absent (),
					Resolution.fatalError);

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
			@NonNull Optional<Integer> userId,
			@NonNull Resolution resolution) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

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
