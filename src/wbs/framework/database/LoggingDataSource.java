package wbs.framework.database;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("loggingDataSource")
public
class LoggingDataSource {

	// singleton dependencies

	@SingletonDependency
	DataSource dataSource;

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	public
	Connection getConnection (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"getConnection");

		) {

			return TaskLogger.implicitArgument.storeAndInvoke (
				taskLogger,
				() -> {

				try {

					return dataSource.getConnection ();

				} catch (SQLException sqlException) {

					throw new RuntimeException (
						sqlException);

				}

			});

		}

	}

}
