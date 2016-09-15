package wbs.framework.hibernate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Provider;

import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import wbs.framework.activitymanager.ActiveTask;
import wbs.framework.activitymanager.ActivityManager;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.database.TransactionView;

@SingletonComponent ("database")
public
class HibernateDatabase
	implements Database {

	// singleton dependencies

	@SingletonDependency
	ActivityManager activityManager;

	@SingletonDependency
	SessionFactory sessionFactory;

	// prototype dependencies

	@PrototypeDependency
	Provider <HibernateInterceptor> hibernateInterceptorProvider;

	// properties

	@Getter @Setter
	boolean allowRepeatedClose = true;

	// state

	ThreadLocal <List <HibernateTransaction>> currentTransactionStackLocal =
		new ThreadLocal <List <HibernateTransaction>> () {

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
			@NonNull String summary,
			@NonNull Object owner,
			boolean readWrite,
			boolean canJoin,
			boolean canCreateNew,
			boolean makeCurrent) {

		ActiveTask activeTask =
			activityManager.start (
				"database",
				summary,
				owner,
				ImmutableMap.of ());

		try {

			HibernateTransaction transaction =
				beginTransactionReal (
					activeTask,
					readWrite,
					canJoin,
					canCreateNew,
					makeCurrent);

			activeTask.put (
				"process id",
				Long.toString (
					transaction.serverProcessId ()));

			return transaction;

		} catch (RuntimeException exception) {

			throw activeTask.fail (
				exception);

		} catch (Error exception) {

			throw activeTask.fail (
				exception);

		} catch (Throwable exception) {

			throw activeTask.fail (
				new RuntimeException (
					exception));

		}

	}

	HibernateTransaction beginTransactionReal (
			ActiveTask transactionTask,
			boolean readWrite,
			boolean canJoin,
			boolean canCreateNew,
			boolean makeCurrent) {

		// check args

		if (! canCreateNew && ! canJoin) {

			throw new IllegalArgumentException (
				"Must specify one of canCreateNew or canJoin");

		}

		try (

			ActiveTask beginTask =
				activityManager.start (
					"database",
					"beginTransactCionReal (...)",
					this);

		) {

			// get current transaction & stack

			List <HibernateTransaction> currentTransactionStack =
				currentTransactionStackLocal.get ();

			@SuppressWarnings ("resource")
			HibernateTransaction currentTransaction =
				currentTransactionStack.size () > 0
					? currentTransactionStack.get (
						currentTransactionStack.size () - 1)
					: null;

			// join an existing transaction

			if (
				currentTransaction != null
				&& canJoin
				&& (
					! readWrite
					|| ! currentTransaction.isReadWrite
				)
			) {

				HibernateTransaction newTransaction =
					new HibernateTransaction (
						this,
						readWrite,
						currentTransaction,
						currentTransactionStack,
						transactionTask);

				newTransaction.begin ();

				if (makeCurrent) {

					currentTransactionStack.add (
						newTransaction);

				}

				return newTransaction;

			}

			// create a new transaction

			if (canCreateNew) {

				HibernateTransaction newTransaction =
					new HibernateTransaction (
						this,
						readWrite,
						null,
						currentTransactionStack,
						transactionTask);

				newTransaction.begin ();

				if (makeCurrent) {

					currentTransactionStack.add (
						newTransaction);

				}

				return newTransaction;

			}

		}

		// throw an appropriate error

		throw new RuntimeException (
			"Unable to begin transaction");

	}

	@Override
	public
	Transaction beginReadWrite (
			@NonNull String summary,
			@NonNull Object owner) {

		return beginTransaction (
			summary,
			owner,
			true,
			false,
			true,
			true);

	}

	@Override
	public
	Transaction beginReadOnly (
			@NonNull String summary,
			@NonNull Object owner) {

		return beginTransaction (
			summary,
			owner,
			false,
			true,
			true,
			true);

	}

	@Override
	public
	Transaction beginReadOnlyJoin (
			@NonNull String summary,
			@NonNull Object owner) {

		return beginTransaction (
			summary,
			owner,
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

		if (currentTransaction == null) {

			throw new RuntimeException (
				"No current transaction");

		}

		return currentTransaction.getSession ();

	}

	@Override
	public
	void flush () {

		Session session =
			currentSession ();

		session.flush ();

	}

	@Override
	public
	void clear () {

		Session session =
			currentSession ();

		session.clear ();

	}

	@Override
	public
	void flushAndClear () {

		flush ();

		clear ();

	}

}
