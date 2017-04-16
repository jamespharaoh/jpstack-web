package wbs.framework.hibernate;

import static wbs.utils.collection.MapUtils.mapToProperties;
import static wbs.utils.etc.LogicUtils.booleanToYesNo;

import java.util.Properties;

import javax.inject.Provider;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import org.hibernate.SessionFactory;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("hibernateComponents")
public
class HibernateComponents {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	WbsConfig wbsConfig;

	// prototype dependencies

	@PrototypeDependency
	Provider <HibernateSessionFactoryBuilder>
		hibernateSessionFactoryBuilderProvider;

	// components

	@SingletonComponent ("hibernateSessionFactory")
	public
	SessionFactory hibernateSessionFactory (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"hibernateSessionFactory");

		Properties configProperties =
			mapToProperties (
				ImmutableMap.<String, String> builder ()

			.put (
				"hibernate.dialect",
				"org.hibernate.dialect.PostgreSQLDialect")

			.put (
				"hibernate.show_sql",
				booleanToYesNo (
					wbsConfig.database ().showSql ()))

			.put (
				"hibernate.format_sql",
				booleanToYesNo (
					wbsConfig.database ().formatSql ()))

			.put (
				"hibernate.connection.isolation",
				"8")

			.build ()

		);

		return hibernateSessionFactoryBuilderProvider.get ()

			.configProperties (
				configProperties)

			.build (
				taskLogger);

	}

}
