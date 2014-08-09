package wbs.framework.hibernate;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;

@SuppressWarnings ("serial")
public
class WbsConnectionProvider
	implements ConnectionProvider {

	DataSource dataSource;

	static
	ThreadLocal<DataSource> dataSourceThreadLocal =
		new ThreadLocal<DataSource> ();

	public WbsConnectionProvider () {

		dataSource =
			dataSourceThreadLocal.get ();

		dataSourceThreadLocal.remove ();

	}

	public static void setDataSource (
			DataSource dataSource) {

		if (dataSourceThreadLocal.get () != null)
			throw new IllegalStateException ();

		dataSourceThreadLocal.set (
			dataSource);

	}

	@Override
	@SuppressWarnings ("rawtypes")
	public boolean isUnwrappableAs (
			Class clazz) {

		return false;

	}

	@Override
	public <T> T unwrap (
			Class<T> arg0) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	void closeConnection (
			Connection connection)
		throws SQLException {

		connection.close ();

	}

	@Override
	public
	Connection getConnection ()
		throws SQLException {

		return dataSource.getConnection ();

	}

	@Override
	public
	boolean supportsAggressiveRelease () {
		return false;
	}

}
