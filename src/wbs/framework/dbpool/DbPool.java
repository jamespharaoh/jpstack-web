package wbs.framework.dbpool;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

/**
 * TODO a maximum connection life would be nice
 * TODO improve thread safety etc
 * TODO logging and debugging
 * TODO expose workings via ui
 * TODO handle errors better
 * TODO testing
 * TODO handle auto commit better
 *
 * @author James Pharaoh
 */
@Accessors (fluent = true)
@Log4j
public
class DbPool
	implements DataSource {

	Class<?> proxyClass;

	Constructor<?> proxyConstructor;

	@PostConstruct
	public
	void init ()
		throws
			NoSuchMethodException,
			SecurityException {

		proxyClass =
			Proxy.getProxyClass (
			Connection.class.getClassLoader (),
				new Class [] { Connection.class });

		proxyConstructor =
			proxyClass.getConstructor (InvocationHandler.class);

	}

	/** A lock to synchronize concurrent access to the connection pool. */
	Object lock =
		new Object ();

	/** The DataSource we get "real" connections from. */

	@Getter @Setter
	DataSource dataSource;

	/** Contains all connections which are not currently in use. */

	Set<ConnectionStuff> idleConnections =
		new TreeSet<ConnectionStuff> ();

	/** Contains all connections which are currently in use. */

	Set<ConnectionStuff> usedConnections =
		new TreeSet<ConnectionStuff> ();

	/** Maximum time to let connections remain idle. */

	long maxIdleTime =
		15 * 1000;

	/** Minimum time between idle connection checks. */

	long idleCheckInterval =
		5 * 1000;

	/** Time at which we close idle connections. */

	long nextIdleCheck;

	/** Max connections to open at any one time. */

	long maxConnections =
		50;

	/**
	 * TODO i think this need more error checking, if the getConnection fails
	 * then we leak something.
	 */
	@Override
	public
	Connection getConnection ()
		throws SQLException {

		ConnectionStuff connectionStuff;

		synchronized (lock) {

			// wait for a connection

			while (idleConnections.isEmpty ()
					&& usedConnections.size () == maxConnections) {

				try {

					lock.wait ();

				} catch (InterruptedException exception) {

					throw new RuntimeException (exception);

				}

			}

			// either use an existing one

			if (! idleConnections.isEmpty ()) {

				connectionStuff =
					idleConnections.iterator ().next ();

				idleConnections.remove (connectionStuff);
				usedConnections.add (connectionStuff);

				return connectionStuff.createClientConnection ();

			}

			// we are going to create a new one

			connectionStuff = new ConnectionStuff ();
			usedConnections.add (connectionStuff);

		}

		// create a new connection

		connectionStuff.realConnection =
			dataSource.getConnection ();

		connectionStuff.realConnection.setTransactionIsolation (
			Connection.TRANSACTION_SERIALIZABLE);

		return connectionStuff.createClientConnection ();

	}

	void closeIdle () {

		long now =
			System.currentTimeMillis ();

		List<Connection> connectionsToClose =
			new ArrayList<Connection> ();

		synchronized (lock) {

			// if not enough time has passed, don't bother

			if (now < nextIdleCheck)
				return;

			// collect collections to close

			Iterator<ConnectionStuff> iterator =
				idleConnections.iterator ();

			while (iterator.hasNext ()) {

				ConnectionStuff connectionStuff =
					iterator.next ();

				if (now < connectionStuff.idleExpiryTime) {

					connectionsToClose.add (
						connectionStuff.realConnection);

					iterator.remove ();

				}

			}

			// schedule the next check

			nextIdleCheck =
				now + idleCheckInterval;

			/*
			// dump info about idle connections

			for (ConnectionStuff connectionStuff
					: usedConnections) {

				logger.info (
					"Connection " +
					connectionStuff.serial +
					" in use by thread " +
					connectionStuff.clientThread.getName () +
					" (" +
					connectionStuff.clientThread.getId () +
					")");

				System.out.println ("---- BEGIN STACK TRACE:");

				for (StackTraceElement elem : connectionStuff.clientStackTrace)
					System.out.println (elem.toString ());

				System.out.println ("---- CURRENT STACK TRACE:");

				for (StackTraceElement elem : connectionStuff.clientThread.getStackTrace ())
					System.out.println (elem.toString ());

			}
			*/

		}

		// close connections
		// TODO do this in a separate thread or something

		for (Connection connection
				: connectionsToClose) {

			try {

				connection.close ();

			} catch (SQLException exception) {

				log.fatal (
					"Unable to close idle connection",
					exception);

			}

		}

	}

	@Override
	public
	Connection getConnection (
			String username,
			String password)
		throws SQLException {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	PrintWriter getLogWriter ()
		throws SQLException {

		throw new UnsupportedOperationException ();

	}


	@Override
	public
	void setLogWriter (
			PrintWriter out) throws SQLException {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	void setLoginTimeout (
			int seconds)
		throws SQLException {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	int getLoginTimeout ()
		throws SQLException {

		throw new UnsupportedOperationException ();

	}

	int nextSerial = 0;

	class ConnectionStuff
		implements
			Comparable<ConnectionStuff>,
			InvocationHandler {

		int serial = nextSerial++;
		long idleExpiryTime;

		Connection realConnection;

		Connection clientConnection;
		Thread clientThread;
		StackTraceElement[] clientStackTrace;

		ConnectionStuff () {
		}

		Connection createClientConnection () {

			if (clientConnection != null)
				throw new IllegalStateException ();

			if (clientThread != null)
				throw new IllegalStateException ();

			if (clientStackTrace != null)
				throw new IllegalStateException ();

			try {

				clientConnection =
					(Connection)
					proxyConstructor.newInstance (this);

			} catch (Exception exception) {

				throw new RuntimeException (exception);

			}

			clientThread =
				Thread.currentThread ();

			clientStackTrace =
				Thread.currentThread ().getStackTrace ();

			return clientConnection;

		}

		void release () {

			synchronized (lock) {

				if (clientConnection == null)
					throw new IllegalStateException ();

				if (clientThread != Thread.currentThread ())
					throw new IllegalStateException ();

				clientConnection = null;
				clientThread = null;
				clientStackTrace = null;

				usedConnections.remove (this);
				idleConnections.add (this);

				idleExpiryTime =
					System.currentTimeMillis () + maxIdleTime;

				// TODO move this to a separate thread, or something
				closeIdle ();

			}

		}

		@Override
		public
		int compareTo (
				ConnectionStuff other) {

			if (other.serial < serial)
				return -1;

			if (serial < other.serial)
				return 1;

			return 0;

		}

		@Override
		public
		Object invoke (
				Object proxy,
				Method method,
				Object[] args)
			throws Throwable {

			if (proxy != clientConnection)
				return null;

			if ("close".equals (method.getName ())) {

				realConnection.setAutoCommit (true);

				release ();

				return null;

			} else if ("isClosed".equals (method.getName ())) {

				return proxy != clientConnection;

			} else {

				return method.invoke (
					realConnection,
					args);

			}

		}

	}

	@Override
	public
	boolean isWrapperFor (
			Class<?> wrappedClass)
		throws SQLException {

		if (wrappedClass == DataSource.class)
			return true;

		return dataSource.isWrapperFor (
			wrappedClass);

	}

	@Override
	public <T>
	T unwrap (
			Class<T> wrappedClass)
		throws SQLException {

		if (wrappedClass == DataSource.class)
			return wrappedClass.cast (this);

		return dataSource.unwrap (
			wrappedClass);

	}

	@Override
	public 
	Logger getParentLogger ()
		throws SQLFeatureNotSupportedException {

		return dataSource.getParentLogger ();

	}

}
