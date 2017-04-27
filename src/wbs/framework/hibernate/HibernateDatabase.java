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
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.BorrowedTransaction;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("database")
public
class HibernateDatabase
	implements Database {

	// singleton dependencies

	@SingletonDependency
	ActivityManager activityManager;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	SessionFactory sessionFactory;

	// prototype dependencies

	@PrototypeDependency
	Provider <HibernateInterceptor> hibernateInterceptorProvider;

	@PrototypeDependency
	Provider <HibernateTransaction> hibernateTransactionProvider;

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

	@SuppressWarnings ("resource")
	@Override
	public
	OwnedTransaction beginTransaction (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String summary,
			@NonNull Object owner,
			boolean readWrite,
			boolean canJoin,
			boolean canCreateNew,
			boolean makeCurrent) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"beginTransaction");

		) {

			ActiveTask activeTask =
				activityManager.start (
					"database",
					summary,
					owner,
					ImmutableMap.of ());

			try {

				HibernateTransaction transaction =
					beginTransactionReal (
						taskLogger,
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

	}

	HibernateTransaction beginTransactionReal (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ActiveTask transactionTask,
			boolean readWrite,
			boolean canJoin,
			boolean canCreateNew,
			boolean makeCurrent) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"beginTransactionReal");

		) {

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
						hibernateTransactionProvider.get ()

						.parentTaskLogger (
							taskLogger)

						.hibernateDatabase (
							this)

						.isReadWrite (
							readWrite)

						.realTransaction (
							currentTransaction)

						.stack (
							currentTransactionStack)

						.activeTask (
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
						hibernateTransactionProvider.get ()

						.parentTaskLogger (
							taskLogger)

						.hibernateDatabase (
							this)

						.id (
							Transaction.IdGenerator.nextId ())

						.isReadWrite (
							readWrite)

						.stack (
							currentTransactionStack)

						.activeTask (
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

	}

	@Override
	public
	BorrowedTransaction currentTransaction () {

		return new BorrowedTransaction (
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

	@SuppressWarnings ("resource")
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

		@SuppressWarnings ("resource")
		Session session =
			currentSession ();

		session.flush ();

	}

	@Override
	public
	void clear () {

		@SuppressWarnings ("resource")
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
