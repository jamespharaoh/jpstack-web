package wbs.framework.exception;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.emptyStringIfNull;
import static wbs.utils.string.StringUtils.stringFormat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import org.hibernate.JDBCException;
import org.hibernate.exception.ConstraintViolationException;
import org.json.simple.JSONObject;

import wbs.framework.component.annotations.SingletonComponent;

@Log4j
@SingletonComponent ("exceptionUtils")
public
class ExceptionUtilsImplementation
	implements ExceptionUtils {

	@Override
	public
	String throwableSummary (
			@NonNull Throwable originalThrowable) {

		Throwable currentThrowable =
			originalThrowable;

		try {

			StringBuilder stringBuilder =
				new StringBuilder ();

			for (;;) {

				stringBuilder.append (
					currentThrowable.toString ());

				currentThrowable =
					currentThrowable.getCause ();

				if (
					isNull (
						currentThrowable)
				) {
					return stringBuilder.toString ();
				}

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

		if (throwable instanceof DetailedException) {

			DetailedException detailedException =
				genericCastUnchecked (
					throwable);

			detailedException.details ().entrySet ().forEach (
				detailsEntry -> {

				printWriter.print (
					stringFormat (
						"\n%s:\n\n",
						detailsEntry.getKey ()));

				detailsEntry.getValue ().forEach (
					detailLine ->
						printWriter.print (
							stringFormat (
								"%s\n",
								detailLine)));

			});

		}

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
			@NonNull SQLException originalSqlException,
			@NonNull PrintWriter out) {

		out.write (
			"\nSQL EXCEPTION:\n\n");

		SQLException currentSqlException =
			originalSqlException;

		while (currentSqlException != null) {

			out.write (
				currentSqlException.getMessage () + "\n");

			currentSqlException =
				currentSqlException.getNextException ();

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

	@Override
	public
	JSONObject throwableDumpJson (
			@NonNull Throwable throwable) {

		ImmutableMap.Builder<String,Object> dumpBuilder =
			ImmutableMap.<String,Object>builder ();

		// class

		dumpBuilder.put (
			"class",
			throwable.getClass ().getName ());

		// message

		if (
			isNotNull (
				throwable.getMessage ())
		) {

			dumpBuilder.put (
				"message",
				emptyStringIfNull (
					throwable.getMessage ()));

		}

		// stack trace

		dumpBuilder.put (
			"stacktrace",
			Arrays.asList (
				throwable.getStackTrace ())

				.stream ()

				.map (
					Object::toString)

				.collect (
					Collectors.toList ())

		);

		// cause

		if (
			isNotNull (
				throwable.getCause ())
		) {

			dumpBuilder.put (
				"cause",
				throwableDumpJson (
					throwable.getCause ()));

		}

		// return

		return new JSONObject (
			dumpBuilder.build ());

	}

}
