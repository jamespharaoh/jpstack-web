package wbs.platform.exception.logic;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import org.hibernate.JDBCException;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.GlobalId;
import wbs.platform.exception.model.ExceptionLogObjectHelper;
import wbs.platform.exception.model.ExceptionLogRec;
import wbs.platform.exception.model.ExceptionLogTypeObjectHelper;
import wbs.platform.exception.model.ExceptionLogTypeRec;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

import com.google.common.base.Optional;

@SingletonComponent ("exceptionLogLogic")
@Log4j
public
class ExceptionLogLogicImpl
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
	ExceptionLogRec logSimple (
			final @NonNull String typeCode,
			final @NonNull String source,
			final @NonNull String summary,
			final @NonNull String dump,
			final @NonNull Optional<Integer> userId,
			final @NonNull Boolean fatal) {

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
					fatal);

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
			final @NonNull Boolean fatal) {

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
					throwableSummary (throwable),
					throwableDump (throwable),
					userId,
					fatal);

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
			final @NonNull Boolean fatal) {

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
					summary + "\n" + throwableSummary (throwable),
					throwableDump (throwable),
					userId,
					fatal);

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

				logThrowableWithSummary (
					typeCode,
					"exception log",
					furtherSummary,
					furtherException,
					Optional.<Integer>absent (),
					true);

			} catch (Exception yetAnotherException) {

				log.fatal (
					"Error logging error logging exception",
					yetAnotherException);

				realLogException (
					typeCode,
					"exception log",
					furtherSummary,
					"",
					userId,
					true);

			}

			return null;

		}

	}

	ExceptionLogRec realLogException (
			@NonNull String typeCode,
			@NonNull String source,
			@NonNull String summary,
			@NonNull String dump,
			@NonNull Optional<Integer> userId,
			@NonNull Boolean fatal) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

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
				substituteNuls (
					summary))

			.setUser (
				user)

			.setDump (
				substituteNuls (
					dump))

			.setFatal (
				fatal)

		);

		transaction.commit ();

		return exceptionLog;

	}

	public static
	String throwableStackTrace (
			@NonNull Throwable throwable) {

		try {

			StringWriter stringWriter =
				new StringWriter ();

			PrintWriter printWriter =
				new PrintWriter (stringWriter);

			throwable.printStackTrace (
				printWriter);

			printWriter.flush ();

			return stringWriter.toString ();

		} catch (Exception exception) {

			log.error (
				"Threw error in throwableStackTrace",
				exception);

			return "(error)";

		}

	}

	@Override
	public
	String throwableSummary (
			@NonNull Throwable throwable) {

		try {

			StringBuilder stringBuilder =
				new StringBuilder ();

			for (;;) {

				stringBuilder.append (
					throwable.toString ());

				throwable =
					throwable.getCause ();

				if (throwable == null)
					return stringBuilder.toString ();

				stringBuilder.append ("\n");

			}

		} catch (Exception exception) {

			log.error (
				"Threw error in throwableSummary",
				exception);

			return "(error)";

		}

	}

	@Override
	public
	String throwableDump (
			@NonNull Throwable throwable) {

		try {

			StringWriter stringWriter =
				new StringWriter ();

			PrintWriter printWriter =
				new PrintWriter (stringWriter);

			writeThrowable (
				throwable,
				printWriter);

			printWriter.flush ();

			return stringWriter.toString ();

		} catch (Exception exception) {

			log.error (
				"Threw error in throwableDump",
				exception);

			return "(error)";

		}

	}

	private static
	void writeThrowable (
			@NonNull Throwable throwable,
			@NonNull PrintWriter printWriter) {

		throwable.printStackTrace (
			printWriter);

		if (throwable instanceof JDBCException) {

			JDBCException jdbcException =
				(JDBCException) throwable;

			printWriter.print (
				"\nSQL:\n\n");

			printWriter.print (
				jdbcException.getSQL ());

			writeSqlException (
				jdbcException.getSQLException (),
				printWriter);

		}

		Throwable cause =
			throwable.getCause ();

		if (cause != null) {

			printWriter.print (
				"\nCAUSE:\n\n");

			writeThrowable (
				cause,
				printWriter);

		}

	}

	private static
	void writeSqlException (
			@NonNull SQLException sqlException,
			@NonNull PrintWriter out) {

		out.write (
			"\nSQL EXCEPTION:\n\n");

		while (sqlException != null) {

			out.write (
				sqlException.getMessage () + "\n");

			sqlException =
				sqlException.getNextException ();

		}

	}

	private static
	String substituteNuls (
			@NonNull String source,
			@NonNull String replacement) {

		return source.replaceAll (
			"\u0000",
			replacement);

	}

	private static
	String substituteNuls (
			@NonNull String source) {

		return substituteNuls (
			source,
			"<NUL>");

	}

}
