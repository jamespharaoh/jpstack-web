package wbs.platform.exception.logic;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;

import javax.inject.Inject;

import lombok.Cleanup;
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

@SingletonComponent ("exceptionLogic")
@Log4j
public
class ExceptionLogicImpl
	implements ExceptionLogic {

	@Inject
	Database database;

	@Inject
	ExceptionLogObjectHelper exceptionLogHelper;

	@Inject
	ExceptionLogTypeObjectHelper exceptionLogTypeHelper;

	@Inject
	UserObjectHelper userHelper;

	@Override
	public
	void logSimple (
			String typeCode,
			String source,
			String summary,
			String dump,
			Integer userId,
			boolean fatal) {

		try {

			realLogException (
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

		}

	}

	@Override
	public
	void logThrowable (
			String typeCode,
			String source,
			Throwable throwable,
			Integer userId,
			boolean fatal) {

		try {

			realLogException (
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

		}

	}

	@Override
	public
	void logThrowableWithSummary (
			String typeCode,
			String source,
			String summary,
			Throwable throwable,
			Integer userId,
			boolean fatal) {

		try {

			realLogException (
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

		}

	}

	private
	void realLogException (
			String typeCode,
			String source,
			String summary,
			String dump,
			Integer userId,
			boolean fatal) {

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
			userId != null
				? userHelper.find (userId)
				: null;

		// create exception log

		exceptionLogHelper.insert (
			new ExceptionLogRec ()
				.setType (exceptionLogType)
				.setSource (source)
				.setSummary (summary)
				.setUser (user)
				.setDump (dump)
				.setFatal (fatal));

		transaction.commit ();

	}

	public static
	String throwableStackTrace (
			Throwable throwable) {

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
			Throwable throwable) {

		StringBuffer buf =
			new StringBuffer ();

		while (true) {

			buf.append (
				throwable.toString ());

			throwable =
				throwable.getCause ();

			if (throwable == null)
				return buf.toString ();

			buf.append ("\n");

		}

	}

	public static
	String throwableDump (
			Throwable throwable) {

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
			Throwable throwable,
			PrintWriter printWriter) {

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
			SQLException sqlException,
			PrintWriter out) {

		out.write ("\nSQL EXCEPTION:\n\n");

		while (sqlException != null) {

			out.write (sqlException.getMessage () + "\n");

			sqlException =
				sqlException.getNextException ();

		}

	}

}
