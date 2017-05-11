package wbs.framework.hibernate;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

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

	// implementation

	@Override
	public
	OwnedTransaction beginTransaction (
			@NonNull LogContext parentLogContext,
			@NonNull Optional <TaskLogger> parentTaskLogger,
			@NonNull String dynamicContextName,
			@NonNull List <CharSequence> dynamicContextParameters,
			boolean readWrite) {

		OwnedTaskLogger transactionTaskLogger;

		if (
			optionalIsPresent (
				parentTaskLogger)
		) {

			transactionTaskLogger =
				parentLogContext.nestTaskLogger (
					parentTaskLogger,
					dynamicContextName,
					dynamicContextParameters,
					optionalAbsent ());

		} else {

			transactionTaskLogger =
				parentLogContext.createTaskLogger (
					dynamicContextName,
					dynamicContextParameters,
					optionalAbsent ());

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
				readWrite);

		}

	}

	private
	HibernateTransaction beginTransactionReal (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull CloseableTaskLogger transactionTaskLogger,
			boolean readWrite) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"beginTransactionReal");

		) {

			HibernateTransaction newTransaction =
				hibernateTransactionProvider.get ()

				.hibernateDatabase (
					this)

				.id (
					TransactionMethods.IdGenerator.nextId ())

				.isReadWrite (
					readWrite)

				.transactionTaskLogger (
					transactionTaskLogger);

			newTransaction.begin (
				taskLogger);

			return newTransaction;

		}

	}

}
