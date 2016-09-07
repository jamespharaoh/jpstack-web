package wbs.platform.postgresql.data;

import javax.inject.Named;
import javax.sql.DataSource;

import org.postgresql.ds.PGSimpleDataSource;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;

@SingletonComponent ("postgresqlRootDataComponents")
public
class PostgresqlRootDataComponents {

	// singleton dependencies

	@SingletonDependency
	WbsConfig wbsConfig;

	// components


	@SingletonComponent ("rootDataSource")
	@Named
	public
	DataSource rootDataSource () {

		PGSimpleDataSource rootDataSource =
			new PGSimpleDataSource ();

		rootDataSource.setServerName (
			wbsConfig.database ().hostname ());

		rootDataSource.setDatabaseName (
			wbsConfig.database ().databaseName ());

		rootDataSource.setUser (
			wbsConfig.database ().username ());

		rootDataSource.setPassword (
			wbsConfig.database ().password ());

		return rootDataSource;

	}

}
