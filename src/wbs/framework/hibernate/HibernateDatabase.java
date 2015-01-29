package wbs.framework.hibernate;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.joda.time.Instant;

import wbs.framework.activitymanager.ActiveTask;
import wbs.framework.activitymanager.ActivityManager;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.database.TransactionView;

import com.google.common.collect.ImmutableMap;

@SingletonComponent ("database")
@Log4j
public
class HibernateDatabase
	implements Database {

	// dependencies

	@Inject
	ActivityManager activityManager;

	@Inject
	SessionFactory sessionFactory;

	// properties

	@Getter @Setter
	boolean allowRepeatedClose = true;

	// state

	ThreadLocal<List<HibernateTransaction>> currentTransactionStackLocal =
		new ThreadLocal<List<HibernateTransaction>> () {

		@Override
		protected
		List<HibernateTransaction> initialValue () {

			return Collections.synchronizedList (
				new ArrayList<HibernateTransaction> ());

		}

	};

	// implementation

	@Override
	public
	Transaction beginTransaction (
			boolean readWrite,
			boolean canJoin,
			boolean canCreateNew,
			boolean makeCurrent) {

		ActiveTask activeTask =
			activityManager.start (
				"transaction",
				ImmutableMap.<String,Object>builder ()
					.build ());

		// check args

		if (! canCreateNew && ! canJoin) {

			throw activeTask.fail (
				new IllegalArgumentException (
					"Must specify one of canCreateNew or canJoin"));

		}

		// get current transaction & stack

		List<HibernateTransaction> currentTransactionStack =
			currentTransactionStackLocal.get ();

		HibernateTransaction currentTransaction =
			currentTransactionStack.size () > 0 ?
				currentTransactionStack.get (
					currentTransactionStack.size () - 1)
				: null;

		// join an existing transaction

		if (currentTransaction != null
				&& canJoin
				&& (! readWrite
					|| ! currentTransaction.isReadWrite)) {

			HibernateTransaction newTransaction =
				new HibernateTransaction (
					readWrite,
					currentTransaction,
					currentTransactionStack,
					activeTask);

			newTransaction.begin ();

			if (makeCurrent)
				currentTransactionStack.add (newTransaction);

			return newTransaction;

		}

		// create a new transaction

		if (canCreateNew) {

			HibernateTransaction newTransaction =
				new HibernateTransaction (
					readWrite,
					null,
					currentTransactionStack,
					activeTask);

			newTransaction.begin ();

			if (makeCurrent)
				currentTransactionStack.add (newTransaction);

			return newTransaction;

		}

		// throw an appropriate error

		throw activeTask.fail (
			new RuntimeException (
				"Unable to begin transaction"));

	}

	@Override
	public
	Transaction beginReadWrite () {

		return beginTransaction (
			true,
			false,
			true,
			true);

	}

	@Override
	public
	Transaction beginReadOnly () {

		return beginTransaction (
			false,
			true,
			true,
			true);

	}

	@Override
	public
	Transaction beginReadOnlyJoin () {

		return beginTransaction (
			false,
			false,
			true,
			true);

	}

	@Override
	public
	Transaction currentTransaction () {
		return new TransactionView (
			currentTransactionReal ());
	}

	private
	HibernateTransaction currentTransactionReal () {

		List<HibernateTransaction> currentTransactionStack =
			currentTransactionStackLocal.get ();

		if (currentTransactionStack.size () == 0)
			return null;

		return currentTransactionStack.get (
			currentTransactionStack.size () - 1);

	}

	public
	Session currentSession () {

		HibernateTransaction currentTransaction =
			currentTransactionReal ();

		if (currentTransaction == null)
			throw new RuntimeException ("No current transaction");

		return currentTransaction.getSession ();

	}

	/**
	 * Represents a hibernate transaction and associated session.
	 *
	 * @author james //
	 */
	private
	class HibernateTransaction
		implements Transaction {

		private final
		long id;

		private final
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

		Map<String,Object> meta =
			new HashMap<String,Object> ();

		private
		HibernateTransaction (
				boolean isReadWrite,
				HibernateTransaction realTransaction,
				List<HibernateTransaction> stack,
				ActiveTask activeTask) {

			this.id = Transaction.IdGenerator.nextId ();
			this.isReadWrite = isReadWrite;
			this.realTransaction = realTransaction;
			this.stack = stack;
			this.activeTask = activeTask;

		}

		private
		void begin () {

			if (realTransaction != null)
				return;

			log.debug (
				stringFormat (
					"BEGIN %d %s",
					id,
					isReadWrite ? "rw" : "ro"));

			session =
				sessionFactory.openSession ();

			hibernateTransaction =
				session.beginTransaction ();

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

			if (stack != null)
				stack.remove (this);

			// do the commit

			if (hibernateTransaction != null)
				hibernateTransaction.commit ();

			committed = true;

		}

		@Override
		public
		void close () {

			// handle repeated close

			if (closed) {

				if (allowRepeatedClose) {

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

				if (session != null)
					session.close ();

				// always close the active task

				activeTask.close ();

			}

		}

		public
		Session getSession () {

			if (closed) {

				throw new RuntimeException (
					"Transaction has been closed");

			}

			if (realTransaction != null)
				return realTransaction.getSession ();

			return session;

		}

		@Override
		protected
		void finalize ()
			throws Throwable {

			if (! closed) {

				log.warn ("Finalising un-closed transaction");

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

			return (int) (id ^ (id >>> 32)); // from Long.hashCode

		}

		@Override
		public
		void flush () {

			session.flush ();

		}

		@Override
		public
		void refresh (
				Object object) {

			session.refresh (
				object);

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

	}

	@Override
	public
	void flush () {

		Session sess =
			currentSession ();

		sess.flush ();

	}

	@Override
	public
	void clear () {

		Session sess =
			currentSession ();

		sess.clear ();

	}

	@Override
	public
	void flushAndClear () {

		flush ();

		clear ();

	}

}
