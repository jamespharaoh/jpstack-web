package wbs.framework.hibernate;

import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import org.hibernate.SessionFactory;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.database.TransactionMethods;
import wbs.framework.logging.CloseableTaskLogger;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("database")
public
class HibernateDatabase
	implements Database {

	// singleton dependencies

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
		List <HibernateTransaction> initialValue () {

			return Collections.synchronizedList (
				new ArrayList<> ());

		}

	};

	// implementation

	@Override
	public
	OwnedTransaction beginTransaction (
			@NonNull LogContext parentLogContext,
			@NonNull Optional <TaskLogger> parentTaskLogger,
			@NonNull String summary,
			boolean readWrite,
			boolean canJoin,
			boolean canCreateNew,
			boolean makeCurrent) {

		OwnedTaskLogger transactionTaskLogger;

		if (
			optionalIsPresent (
				parentTaskLogger)
		) {

			transactionTaskLogger =
				logContext.nestTaskLogger (
					optionalGetRequired (
						parentTaskLogger),
					"beginTransaction");

		} else {

			transactionTaskLogger =
				logContext.createTaskLogger (
					"beginTransaction");

		}

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					transactionTaskLogger,
					"beginTransaction");

		) {

			return beginTransactionReal (
				taskLogger,
				transactionTaskLogger,
				readWrite,
				canJoin,
				canCreateNew,
				makeCurrent);

		}

	}

	@SuppressWarnings ("resource")
	private
	HibernateTransaction beginTransactionReal (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull CloseableTaskLogger transactionTaskLogger,
			boolean readWrite,
			boolean canJoin,
			boolean canCreateNew,
			boolean makeCurrent) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"beginTransactionReal");

		) {

			// check args

			if (! canCreateNew && ! canJoin) {

				throw new IllegalArgumentException (
					"Must specify one of canCreateNew or canJoin");

			}

			// get current transaction & stack

			List <HibernateTransaction> currentTransactionStack =
				currentTransactionStackLocal.get ();

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

					.transactionTaskLogger (
						transactionTaskLogger);

				newTransaction.begin (
					taskLogger);

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
						TransactionMethods.IdGenerator.nextId ())

					.isReadWrite (
						readWrite)

					.stack (
						currentTransactionStack)

					.transactionTaskLogger (
						transactionTaskLogger);

				newTransaction.begin (
					taskLogger);

				if (makeCurrent) {

					currentTransactionStack.add (
						newTransaction);

				}

				return newTransaction;

			}

			// throw an appropriate error

			throw new RuntimeException (
				"Unable to begin transaction");

		}

	}

}
