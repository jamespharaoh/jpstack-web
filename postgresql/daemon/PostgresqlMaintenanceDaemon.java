package wbs.platform.postgresql.daemon;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.sql.DataSource;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;

import org.joda.time.Instant;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.ExceptionLogger.Resolution;
import wbs.platform.daemon.AbstractDaemonService;
import wbs.platform.daemon.ThreadManager;

import com.google.common.base.Optional;

@Log4j
@SingletonComponent ("pgMaint")
public
class PostgresqlMaintenanceDaemon
	extends AbstractDaemonService {

	// dependencies

	@Inject
	DataSource dataSource;

	@Inject
	ExceptionLogger exceptionLogger;

	@Inject
	ThreadManager threadManager;

	// details

	final static
	int monthlyDay = 15;

	final static
	int weeklyDayOfWeek =
		Calendar.WEDNESDAY;

	final static
	int dailyHour = 5;

	final static
	int hourlyMinute = 30;

	@Override
	protected
	String getThreadName () {
		return "PgMaint";
	}

	private
	String frequencyName (
			String frequency) {

		if (frequency.equals ("m"))
			return "monthly";

		if (frequency.equals ("w"))
			return "weekly";

		if (frequency.equals ("d"))
			return "daily";

		if (frequency.equals ("h"))
			return "hourly";

		if (frequency.equals ("5"))
			return "five-minute";

		if (frequency.equals ("1"))
			return "one-minute";

		return frequency;

	}

	private
	void doTasks (
			String frequency) {

		String frequencyName =
			frequencyName (frequency);

		log.info (
			stringFormat (
				"%s maintenance starting",
				frequencyName));

		try {

			@Cleanup
			Connection connnection =
				dataSource.getConnection ();

			connnection.setTransactionIsolation (
				Connection.TRANSACTION_SERIALIZABLE);

			connnection.setAutoCommit (
				true);

			// get list of commands

			List<Command> commands =
				fetchCommands (
					connnection,
					frequency);

			// prepare the update

			String updateQuery =
				"UPDATE postgresql_maintenance " +
				"SET last_run = ?, " +
					"last_duration = ?, " +
					"last_output = ? " +
				"WHERE id = ?";

			@Cleanup
			PreparedStatement updateStatement =
				connnection.prepareStatement (
					updateQuery);

			Statement statement =
				connnection.createStatement ();

			// for each command...

			for (Command command
					: commands) {

				// output a pretty message

				log.debug (
					stringFormat (
						"executing \"%s\"",
						command.command));

				try {

					// perform (and time) the command

					Instant time1 =
						Instant.now ();

					statement.execute (
						command.command);

					Instant time2 =
						Instant.now ();

					// collect output

					StringBuilder stringBuilder =
						new StringBuilder ();

					SQLWarning warning =
						statement.getWarnings ();

					while (warning != null) {

						if (warning.toString () != null) {

							if (stringBuilder.length () > 0)
								stringBuilder.append ("\n");

							stringBuilder.append (
								warning.toString ());

						}

						warning =
							warning.getNextWarning ();

					}

					statement.clearWarnings ();

					// and update the db

					updateStatement.setTimestamp (
						1,
						new Timestamp (
							time1.getMillis ()));

					updateStatement.setLong (
						2,
						time2.getMillis () - time1.getMillis ());

					updateStatement.setString (
						3,
						stringBuilder.toString ());

					updateStatement.setInt (
						4,
						command.id);

					updateStatement.execute ();

				} catch (Exception exception) {

					exceptionLogger.logThrowable (
						"daemon",
						getClass ().getSimpleName (),
						exception,
						Optional.<Integer>absent (),
						Resolution.tryAgainLater);

				}

			}

		} catch (Exception exception) {

			exceptionLogger.logThrowable (
				"daemon",
				getClass ().getSimpleName (),
				exception,
				Optional.<Integer>absent (),
				Resolution.tryAgainLater);

		}

		log.info (
			stringFormat (
				"%s maintenance complete",
				frequencyName));

	}

	List<Command> fetchCommands (
			Connection connection,
			String frequency)
		throws SQLException {

		List<Command> commands =
			new ArrayList<Command> ();

		@Cleanup
		PreparedStatement preparedStatement =
			connection.prepareStatement (
				stringFormat (
					"SELECT id, command ",
					"FROM postgresql_maintenance ",
					"WHERE frequency = ? ",
					"ORDER BY sequence, command"));

		preparedStatement.setString (
			1,
			frequency);

		@Cleanup
		ResultSet resultSet =
			preparedStatement.executeQuery ();

		while (resultSet.next ()) {

			Command command =
				new Command ();

			command.id =
				resultSet.getInt (1);

			command.command =
				resultSet.getString (2);

			commands.add (
				command);

		}

		return commands;

	}

	long nextDaily;
	long nextHourly;
	long nextFiveMins;
	long nextOneMin;

	@Override
	public
	void runService () {

		calculateTimes ();

		while (true) {

			try {
				waitNext ();
			} catch (InterruptedException exception) {
				return;
			}

			doStuff ();

		}

	}

	void doStuff () {

		Calendar calendar =
			Calendar.getInstance ();

		long now =
			calendar.getTime ().getTime ();

		if (now >= nextDaily) {

			if (equal (
					calendar.get (Calendar.DATE),
					monthlyDay)) {

				doTasks ("m");

			} else if (equal (
					calendar.get (Calendar.DAY_OF_WEEK),
					weeklyDayOfWeek)) {

				doTasks ("w");

			} else {

				doTasks ("d");

			}

			calcDaily (now);

		}

		if (now >= nextHourly) {

			doTasks ("h");

			calcHourly (now);

		}

		if (now >= nextFiveMins) {

			doTasks ("5");

			calcFiveMins (now);

		}

		if (now >= nextOneMin) {

			doTasks ("1");

			calcOneMin (now);

		}

	}

	void waitNext ()
		throws InterruptedException {

		long nextTime = nextDaily;

		if (nextHourly < nextTime)
			nextTime = nextHourly;

		if (nextFiveMins < nextTime)
			nextTime = nextFiveMins;

		if (nextOneMin < nextTime)
			nextTime = nextOneMin;

		long now =
			System.currentTimeMillis ();

		if (now < nextTime)
			Thread.sleep (nextTime - now);

	}

	void calculateTimes () {

		long now =
			System.currentTimeMillis ();

		calcDaily (now);
		calcHourly (now);
		calcFiveMins (now);
		calcOneMin (now);

	}

	void calcDaily (
			long now) {

		Calendar calendar =
			Calendar.getInstance ();

		calendar.setTime (
			new Date (now));

		if (calendar.get (Calendar.HOUR_OF_DAY) >= dailyHour) {

			calendar.add (
				Calendar.DATE,
				1);

		}

		calendar.set (
			Calendar.HOUR_OF_DAY,
			dailyHour);

		calendar.set (
			Calendar.MINUTE,
			0);

		calendar.set (
			Calendar.SECOND,
			0);

		calendar.set (
			Calendar.MILLISECOND,
			0);

		nextDaily =
			calendar.getTime ().getTime ();

	}

	void calcHourly (
			long now) {

		Calendar calendar =
			Calendar.getInstance ();

		calendar.setTime (
			new Date (now));

		if (calendar.get (Calendar.MINUTE) >= hourlyMinute) {

			calendar.add (
				Calendar.HOUR_OF_DAY,
				1);

		}

		calendar.set (
			Calendar.MINUTE,
			hourlyMinute);

		calendar.set (
			Calendar.SECOND,
			0);

		calendar.set (
			Calendar.MILLISECOND,
			0);

		nextHourly =
			calendar.getTime ().getTime ();

	}

	void calcFiveMins (
			long now) {

		Calendar calendar =
			Calendar.getInstance ();

		calendar.setTime (
			new Date (now));

		calendar.add (
			Calendar.MINUTE,
			+ 5
			- (
				+ calendar.get (Calendar.MINUTE)
				- 3
			) % 5);

		calendar.set (
			Calendar.SECOND,
			0);

		calendar.set (
			Calendar.MILLISECOND,
			0);

		nextFiveMins =
			calendar.getTime ().getTime ();

	}

	void calcOneMin (
			long now) {

		Calendar calendar =
			Calendar.getInstance ();

		calendar.setTime (
			new Date (now));

		if (calendar.get (Calendar.SECOND) >= 30) {

			calendar.add (
				Calendar.MINUTE,
				1);

		}

		calendar.set (
			Calendar.SECOND,
			30);

		calendar.set (
			Calendar.MILLISECOND,
			0);

		nextOneMin =
			calendar.getTime ().getTime ();

	}

	static
	class Command {
		int id;
		String command;
	}

}