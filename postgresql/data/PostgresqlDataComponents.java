package wbs.platform.postgresql.data;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.sql.DataSource;

import org.postgresql.ds.PGSimpleDataSource;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.annotations.UninitializedDependency;
import wbs.framework.application.config.WbsConfig;
import wbs.framework.database.DbPool;

@SingletonComponent ("postgresqlDataComponents")
public
class PostgresqlDataComponents {

	// dependencies

	@Inject
	WbsConfig wbsConfig;

	// indirect dependencies

	@Inject @Named
	Provider <DataSource> rootDataSource;

	// prototype depdencies

	@UninitializedDependency
	Provider <DbPool> dbPoolProvider;

	// components

	@SingletonComponent ("dataSource")
	public
	DataSource dataSource () {

		return dbPoolProvider.get ()

			.dataSource (
				rootDataSource.get ());

	}

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
