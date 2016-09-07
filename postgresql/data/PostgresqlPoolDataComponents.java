package wbs.platform.postgresql.data;

import javax.inject.Named;
import javax.inject.Provider;
import javax.sql.DataSource;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.UninitializedDependency;
import wbs.framework.database.DbPool;

@SingletonComponent ("postgresqlPoolDataComponents")
public
class PostgresqlPoolDataComponents {

	// singleton dependencies

	@SingletonDependency
	@Named
	DataSource rootDataSource;

	// prototype depdencies

	@UninitializedDependency
	Provider <DbPool> dbPoolProvider;

	// components

	@SingletonComponent ("dataSource")
	public
	DataSource dataSource () {

		return dbPoolProvider.get ()

			.dataSource (
				rootDataSource);

	}

}
