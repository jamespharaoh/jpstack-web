package wbs.framework.exception;

import static wbs.framework.utils.etc.Misc.isNotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import org.hibernate.JDBCException;
import org.hibernate.exception.ConstraintViolationException;

import wbs.framework.application.annotations.SingletonComponent;

@Log4j
@SingletonComponent ("exceptionUtils")
public
class ExceptionUtilsImplementation
	implements ExceptionUtils {

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

	public static
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

			printWriter.print (
				"\n");

			if (throwable instanceof ConstraintViolationException) {

				ConstraintViolationException constraintViolationException =
					(ConstraintViolationException)
					jdbcException;

				if (
					isNotNull (
						constraintViolationException.getConstraintName ())
				) {

					printWriter.print (
						"\nCONSTRAINT:\n\n");

					printWriter.print (
						constraintViolationException.getConstraintName ());

					printWriter.print (
						"\n");

				}

			}

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

	public static
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

	public static
	String substituteNuls (
			@NonNull String source,
			@NonNull String replacement) {

		return source.replaceAll (
			"\u0000",
			replacement);

	}

	public static
	String substituteNuls (
			@NonNull String source) {

		return substituteNuls (
			source,
			"<NUL>");

	}

}
