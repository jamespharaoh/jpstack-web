package wbs.framework.hibernate;

import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.jdbc.ReturningWork;
import org.joda.time.Instant;

import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;
import wbs.framework.activitymanager.ActiveTask;
import wbs.framework.database.Transaction;
import wbs.framework.database.WbsConnection;
import wbs.framework.record.UnsavedRecordDetector;

@Accessors (fluent = true)
@Log4j
class HibernateTransaction
	implements Transaction {

	private final
	HibernateDatabase hibernateDatabase;

	private final
	long id;

	final
	boolean isReadWrite;

	private final
	HibernateTransaction realTransaction;

	private final
	List<HibernateTransaction> stack;

	private final
	ActiveTask activeTask;

	private final
	Instant now =
		Instant.now ();

	private boolean
	committed, closed;

	private
	Session session;

	private
	org.hibernate.Transaction hibernateTransaction;

	@Getter
	long serverProcessId;

	Map<String,Object> meta =
		new HashMap<String,Object> ();

	HibernateTransaction (
			HibernateDatabase hibernateDatabase,
			boolean isReadWrite,
			HibernateTransaction realTransaction,
			List<HibernateTransaction> stack,
			ActiveTask activeTask) {

		this.hibernateDatabase = hibernateDatabase;
		this.id = Transaction.IdGenerator.nextId ();
		this.isReadWrite = isReadWrite;
		this.realTransaction = realTransaction;
		this.stack = stack;
		this.activeTask = activeTask;

	}

	void begin () {

		if (
			isNotNull (
				realTransaction)
		) {
			return;
		}

		log.debug (
			stringFormat (
				"BEGIN %d %s",
				id,
				isReadWrite
					? "rw"
					: "ro"));

		// create session

		{

			@Cleanup
			ActiveTask activeTask =
				hibernateDatabase.activityManager.start (
					"database",
					"HibernateTransaction.begin () - create session",
					this);

			session =
				hibernateDatabase.sessionFactory.withOptions ()

				.interceptor (
					hibernateDatabase.hibernateInterceptorProvider.get ())

				.openSession ();

		}

		// begin transaction

		{

			@Cleanup
			ActiveTask activeTask =
				hibernateDatabase.activityManager.start (
					"database",
					"HibernateTransaction.begin () - begin transaction",
					this);

			hibernateTransaction =
				session.beginTransaction ();

		}

		// get server process id

		{

			@Cleanup
			ActiveTask activeTask =
				hibernateDatabase.activityManager.start (
					"database",
					"HibernateTransaction.begin () - get server process id",
					this);

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

	}

	@Override
	public
	void commit () {

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

		log.debug (
			stringFormat (
				"COMMIT %d %s",
				id,
				isReadWrite
					? "rw"
					: "ro"));

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

		if (stack != null)
			stack.remove (this);

		try {

			// rollback if appropriate

			if (hibernateTransaction != null && ! committed) {

				log.debug (
					stringFormat (
						"ROLLBACK %d %s",
						id,
						isReadWrite
							? "rw"
							: "ro"));

				hibernateTransaction.rollback ();

			}

			// finish the active task

			activeTask.success ();

		} finally {

			// always close the session

			try {

				if (session != null) {
					session.close ();
				}

			} catch (Exception exception) {

				log.fatal (
					"Error closing session",
					exception);

			}

			// always close the active task

			try {

				activeTask.close ();

			} catch (Exception exception) {

				log.fatal (
					"Error closing active task",
					exception);

			}

			// always tidy unsaved record detector

			if (
				isNull (
					realTransaction)
			) {

				try {

					UnsavedRecordDetector.instance.destroyFrame (
						this);

				} catch (Exception exception) {

					log.fatal (
						"Error destroying unsaved record detector frame",
						exception);

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

		if (! closed) {

			log.warn (
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