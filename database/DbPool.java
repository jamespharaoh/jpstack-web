package wbs.framework.database;

import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.utils.etc.ReflectionUtils.getConstructor;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.sql.DataSource;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.BorrowedTaskLogger;
import wbs.framework.logging.CloseableTaskLogger;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.etc.ImplicitArgument.BorrowedArgument;

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
@PrototypeComponent ("dbPool")
public
class DbPool
	implements DataSource {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// state

	Class <?> proxyClass;

	Constructor <?> proxyConstructor;

	// life cycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			proxyClass =
				Proxy.getProxyClass (
					getClass ().getClassLoader (),
						new Class [] {
							Connection.class,
							WbsConnection.class,
						});

			proxyConstructor =
				getConstructor (
					proxyClass,
					InvocationHandler.class);

		}

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

		try (

			BorrowedArgument <CloseableTaskLogger, BorrowedTaskLogger>
				taskLoggerProvider =
					TaskLogger.implicitArgument.borrow ();

		) {

			BorrowedTaskLogger taskLogger =
				taskLoggerProvider.get ();

			ConnectionStuff connectionStuff;

			synchronized (lock) {

				// wait for a connection

				waitForConnection (
					taskLogger,
					lock);

				// either use an existing one

				if (! idleConnections.isEmpty ()) {

					connectionStuff =
						idleConnections.iterator ().next ();

					idleConnections.remove (connectionStuff);
					usedConnections.add (connectionStuff);

					return connectionStuff.createClientConnection ();

				}

				// we are going to create a new one

				connectionStuff =
					new ConnectionStuff ();

				usedConnections.add (
					connectionStuff);

			}

			// create a new connection

			return createNewConnection (
				taskLogger,
				connectionStuff);

		}

	}

	private
	void waitForConnection (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Object lock) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"waitForConnection");

		) {

			while (
				idleConnections.isEmpty ()
				&& usedConnections.size () == maxConnections
			) {

				try {

					lock.wait ();

				} catch (InterruptedException exception) {

					Thread.currentThread ().interrupt ();

					throw new RuntimeException (exception);

				}

			}

		}

	}

	private
	Connection createNewConnection (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ConnectionStuff connectionStuff)
		throws SQLException {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createNewConnection");

		) {

			connectionStuff.realConnection =
				TaskLogger.implicitArgument.storeAndInvoke (
					taskLogger,
					() -> {

				try {

					return dataSource.getConnection ();

				} catch (SQLException sqlException) {

					throw new RuntimeException (
						sqlException);

				}

			});

			connectionStuff.realConnection.setTransactionIsolation (
				Connection.TRANSACTION_REPEATABLE_READ);

			return connectionStuff.createClientConnection ();

		}

	}

	void closeIdle () {

		try (

			OwnedTaskLogger taskLogger =
				logContext.createTaskLogger (
					"closeIdle");

		) {

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

					taskLogger.fatalFormatException (
						exception,
						"Unable to close idle connection");

				}

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
		long serverProcessId;

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
					proxyConstructor.newInstance (
						this);

			} catch (Exception exception) {

				throw new RuntimeException (
					exception);

			}

			clientThread =
				Thread.currentThread ();

			clientStackTrace =
				Thread.currentThread ().getStackTrace ();

			return clientConnection;

		}

		void fetchServerProcessId () {

			try (

				Statement statement =
					realConnection.createStatement ();

				ResultSet resultSet =
					statement.executeQuery (
						"SELECT pg_backend_pid ()");

			) {

				if (! resultSet.next ()) {

					shouldNeverHappen ();

				}

				serverProcessId =
					resultSet.getLong (
						1);

			} catch (SQLException exception) {

				throw new RuntimeException (
					exception);

			}

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
				Object[] arguments)
			throws Throwable {

			if (proxy != clientConnection)
				return null;

			Class<?> declaringClass =
				method.getDeclaringClass ();

			if (declaringClass == WbsConnection.class) {

				switch (method.getName ()) {

				case "serverProcessId":

					return serverProcessId;

				default:

					throw shouldNeverHappen ();

				}

			} else {

				switch (method.getName ()) {

				case "close":

					realConnection.setAutoCommit (true);

					release ();

					return null;

				case "isClosed":

					return proxy != clientConnection;

				default:

					return method.invoke (
						realConnection,
						arguments);

				}

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
