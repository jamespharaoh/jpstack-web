package wbs.framework.hibernate;

import static wbs.framework.utils.etc.MapUtils.mapToProperties;
import static wbs.framework.utils.etc.Misc.booleanToYesNo;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Provider;

import org.hibernate.SessionFactory;

import com.google.common.collect.ImmutableMap;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.config.WbsConfig;

@SingletonComponent ("hibernateComponents")
public
class HibernateComponents {

	// dependencies

	@Inject
	Provider <HibernateSessionFactoryBuilder>
		hibernateSessionFactoryBuilderProvider;

	@Inject
	WbsConfig wbsConfig;

	// components

	@SingletonComponent ("hibernateSessionFactory")
	public
	SessionFactory hibernateSessionFactory () {

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

			.build ();

	}

}
