package wbs.platform.exception.logic;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;

import javax.inject.Inject;

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

@SingletonComponent ("exceptionLogic")
@Log4j
public
class ExceptionLogicImpl
	implements ExceptionLogic {

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
			@NonNull String typeCode,
			@NonNull String source,
			@NonNull String summary,
			@NonNull String dump,
			@NonNull Optional<Integer> userId,
			@NonNull Boolean fatal) {

		try {

			return realLogException (
				typeCode,
				source,
				summary,
				dump,
				userId,
				fatal);

		} catch (Exception exception) {

			log.fatal (
				"Unable to exception",
				exception);

			return null;

		}

	}

	@Override
	public
	ExceptionLogRec logThrowable (
			@NonNull String typeCode,
			@NonNull String source,
			@NonNull Throwable throwable,
			@NonNull Optional<Integer> userId,
			@NonNull Boolean fatal) {

		try {

			return realLogException (
				typeCode,
				source,
				throwableSummary (throwable),
				throwableDump (throwable),
				userId,
				fatal);

		} catch (Exception exception) {

			log.fatal (
				"Unable to log exception",
				exception);

			return null;

		}

	}

	@Override
	public
	ExceptionLogRec logThrowableWithSummary (
			@NonNull String typeCode,
			@NonNull String source,
			@NonNull String summary,
			@NonNull Throwable throwable,
			@NonNull Optional<Integer> userId,
			@NonNull Boolean fatal) {

		try {

			return realLogException (
				typeCode,
				source,
				summary + "\n" + throwableSummary (throwable),
				throwableDump (throwable),
				userId,
				fatal);

		} catch (Exception exception) {

			log.fatal (
				"Unable to log exception",
				exception);

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
			database.beginReadWrite ();

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
				summary)

			.setUser (
				user)

			.setDump (
				dump)

			.setFatal (
				fatal)

		);

		transaction.commit ();

		return exceptionLog;

	}

	public static
	String throwableStackTrace (
			@NonNull Throwable throwable) {

		StringWriter stringWriter =
			new StringWriter ();

		PrintWriter printWriter =
			new PrintWriter (stringWriter);

		throwable.printStackTrace (
			printWriter);

		printWriter.flush ();

		return stringWriter.toString ();

	}

	public static
	String throwableSummary (
			@NonNull Throwable throwable) {

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

	}

	public static
	String throwableDump (
			@NonNull Throwable throwable) {

		StringWriter stringWriter =
			new StringWriter ();

		PrintWriter printWriter =
			new PrintWriter (stringWriter);

		writeThrowable (
			throwable,
			printWriter);

		printWriter.flush ();

		return stringWriter.toString ();

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

			printWriter.print ("\nSQL:\n\n");
			printWriter.print (jdbcException.getSQL ());

			writeSqlException (
				jdbcException.getSQLException (),
				printWriter);

		}

		Throwable cause =
			throwable.getCause ();

		if (cause != null) {

			printWriter.print ("\nCAUSE:\n\n");

			writeThrowable (
				cause,
				printWriter);

		}

	}

	private static
	void writeSqlException (
			@NonNull SQLException sqlException,
			@NonNull PrintWriter out) {

		out.write ("\nSQL EXCEPTION:\n\n");

		while (sqlException != null) {

			out.write (sqlException.getMessage () + "\n");

			sqlException =
				sqlException.getNextException ();

		}

	}

}
