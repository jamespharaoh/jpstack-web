package wbs.framework.hibernate;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.jdbc.ReturningWork;
import org.joda.time.Instant;

import wbs.framework.activitymanager.ActiveTask;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.Transaction;
import wbs.framework.database.WbsConnection;
import wbs.framework.entity.record.UnsavedRecordDetector;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("hibernateTransaction")
@Accessors (fluent = true)
public
class HibernateTransaction
	implements Transaction {

	// singleton components

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	TaskLogger parentTaskLogger;

	@Getter @Setter
	HibernateDatabase hibernateDatabase;

	@Getter @Setter
	long id;

	@Getter @Setter
	boolean isReadWrite;

	@Getter @Setter
	HibernateTransaction realTransaction;

	@Getter @Setter
	List<HibernateTransaction> stack;

	@Getter @Setter
	ActiveTask activeTask;

	// state

	@Getter
	long serverProcessId;

	private final
	Instant now =
		Instant.now ();

	private
	Map <String, Object> meta =
		new HashMap<> ();

	private
	boolean begun;

	private
	boolean committed;

	private
	boolean closed;

	private
	boolean unsavedRecordFrameCreated;

	private
	Session session;

	private
	org.hibernate.Transaction hibernateTransaction;

	// implementation

	void begin () {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"begin");

		if (
			isNotNull (
				realTransaction)
		) {
			return;
		}

		taskLogger.debugFormat (
			"BEGIN %s %s",
			integerToDecimalString (
				id),
			isReadWrite
				? "rw"
				: "ro");

		// create session

		try (

			ActiveTask activeTask =
				hibernateDatabase.activityManager.start (
					"database",
					"HibernateTransaction.begin () - create session",
					this);

		) {

			session =
				hibernateDatabase.sessionFactory.withOptions ()

				.interceptor (
					hibernateDatabase.hibernateInterceptorProvider.get ())

				.openSession ();

		}

		// begin transaction

		try (

			ActiveTask activeTask =
				hibernateDatabase.activityManager.start (
					"database",
					"HibernateTransaction.begin () - begin transaction",
					this);

		) {

			hibernateTransaction =
				session.beginTransaction ();

		}

		// get server process id

		try (

			ActiveTask activeTask =
				hibernateDatabase.activityManager.start (
					"database",
					"HibernateTransaction.begin () - get server process id",
					this);

		) {

			serverProcessId =
				session.doReturningWork (
					new ReturningWork<Long> () {

				@Override
				public
				Long execute (
						@NonNull Connection connection)
					throws SQLException {

					WbsConnection wbsConnection =
						(WbsConnection) connection;

					return wbsConnection.serverProcessId ();

				}

			});

		}

		// create unsaved record detector frame

		UnsavedRecordDetector.instance.createFrame (
			this);

		unsavedRecordFrameCreated = true;

		// update state

		begun = true;

	}

	@Override
	public
	void commit () {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"commit");

		// check there is nothing stupid going on

		if (! isReadWrite) {

			throw new RuntimeException (
				"Tried to commit read-only transaction");

		}

		if (committed) {

			throw new RuntimeException (
				"Tried to commit transaction twice");

		}

		if (closed) {

			throw new RuntimeException (
				"Tried to commit closed transaction");

		}

		if (! begun) {

			throw new RuntimeException (
				"Tried to commit unbegun transaction");

		}

		taskLogger.debugFormat (
			"COMMIT %s %s",
			integerToDecimalString (
				id),
			isReadWrite
				? "rw"
				: "ro");

		// remove us from the transaction stack

		if (stack != null) {

			stack.remove (
				this);

		}

		// verify unsaved records

		if (
			isNull (
				realTransaction)
		) {

			UnsavedRecordDetector.instance.verifyFrame (
				this);

		}

		// do the commit

		if (hibernateTransaction != null) {

			hibernateTransaction.commit ();

		}

		committed = true;

	}

	@Override
	public
	void close () {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"close");

		// handle repeated close

		if (closed) {

			if (this.hibernateDatabase.allowRepeatedClose) {

				return;

			} else {

				throw new RuntimeException (
					"Tried to close transaction twice");

			}

		}

		closed = true;

		// remove us from the transaction stack

		if (stack != null) {

			stack.remove (
				this);

		}

		try {

			// rollback if appropriate

			if (hibernateTransaction != null && ! committed) {

				taskLogger.debugFormat (
					"ROLLBACK %s %s",
					integerToDecimalString (
						id),
					isReadWrite
						? "rw"
						: "ro");

				hibernateTransaction.rollback ();

			}

		} finally {

			// always close the session

			try {

				if (session != null) {
					session.close ();
				}

			} catch (Exception exception) {

				taskLogger.fatalFormatException (
					exception,
					"Error teardown session");

			}

			// always close the active task

			try {

				if (
					isNotNull (
						activeTask)
				) {
					activeTask.close ();
				}

			} catch (Exception exception) {

				taskLogger.fatalFormatException (
					exception,
					"Error teardown active task");

			}

			// always tidy unsaved record detector

			if (unsavedRecordFrameCreated) {

				try {

					UnsavedRecordDetector.instance.destroyFrame (
						this);

				} catch (Exception exception) {

					taskLogger.fatalFormatException (
						exception,
						"Error destroying unsaved record detector frame");

				}

			}

		}

	}

	public
	Session getSession () {

		if (closed) {

			throw new IllegalStateException (
				"Transaction has been closed");

		}

		if (realTransaction != null) {
			return realTransaction.getSession ();
		}

		return session;

	}

	@Override
	protected
	void finalize ()
		throws Throwable {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"finalize");

		if (! closed) {

			taskLogger.warningFormat (
				"Finalising un-closed transaction");

			close ();

		}

		super.finalize ();

	}

	@Override
	public
	long getId () {

		return id;

	}

	@Override
	public
	int hashCode () {

		return Long.hashCode (
			id);

	}

	@Override
	public
	void flush () {

		session.flush ();

	}

	@Override
	public
	void refresh (
			Object... objects) {

		session.flush ();

		for (
			Object object
				: objects
		) {

			session.refresh (
				object);

		}

	}

	@Override
	public
	Instant now () {

		return now;

	}

	@Override
	public
	void setMeta (
			String key,
			Object value) {

		meta.put (
			key,
			value);

	}

	@Override
	public
	Object getMeta (
			String key) {

		return meta.get (
			key);

	}

	@Override
	public
	void fetch (
			Object... objects) {

		for (Object object : objects) {

			Hibernate.initialize (
				object);

		}

	}

	@Override
	public
	boolean contains (
			Object... objects) {

		for (
			Object object
				: objects
		) {

			if (! session.contains (object)) {
				return false;
			}

		}

		return true;

	}

}