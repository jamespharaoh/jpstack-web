package wbs.platform.deployment.daemon;

import static wbs.utils.etc.NetworkUtils.runHostname;
import static wbs.utils.string.StringUtils.objectToString;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;

import java.util.List;

import lombok.NonNull;

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.joda.time.Duration;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.daemon.SleepingDaemonService;
import wbs.platform.deployment.model.ConsoleDeploymentObjectHelper;
import wbs.platform.deployment.model.ConsoleDeploymentRec;
import wbs.platform.deployment.model.DeploymentState;

@SingletonComponent ("deploymentDaemon")
public
class DeploymentAgent
	extends SleepingDaemonService {

	// singleton components

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleDeploymentObjectHelper consoleDeploymentHelper;

	// details

	@Override
	protected
	Duration getSleepDuration () {
		return Duration.standardSeconds (1);
	}

	@Override
	protected
	String getThreadName () {
		return "DeploymentAgent";
	}

	@Override
	protected
	String generalErrorSource () {
		return "deployment agent";
	}

	@Override
	protected
	String generalErrorSummary () {
		return "Error checking deployment state";
	}

	// state

	private
	String hostname =
		runHostname ();

	// implementation

	@Override
	protected
	void runOnce (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"runOnce");

		try (

			Transaction transaction =
				database.beginReadWrite (
					"runOnce ()",
					this);

		) {

			// find local console deployments

			List <ConsoleDeploymentRec> consoleDeployments =
				consoleDeploymentHelper.findByHostNotDeleted (
					hostname);

			consoleDeployments.forEach (
				consoleDeployment ->
					runConsoleDeployment (
						taskLogger,
						transaction,
						consoleDeployment));

			// commit transaction

			transaction.commit ();

		}

	}

	// private implementation

	private
	void runConsoleDeployment (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Transaction transaction,
			@NonNull ConsoleDeploymentRec consoleDeployment) {

		TaskLogger taskLogger =
			logContext.nestTaskLoggerFormat (
				parentTaskLogger,
				"runConsoleDeployment (%s)",
				objectToString (
					consoleDeployment));

		// update state

		try {

			consoleDeployment

				.setState (
					getServiceState (
						taskLogger,
						consoleDeployment.getServiceName ()))

				.setStateTimestamp (
					transaction.now ());

		} catch (Exception exception) {

			consoleDeployment

				.setState (
					DeploymentState.unknown)

				.setStateTimestamp (
					transaction.now ());

		}

		// perform restart

		if (consoleDeployment.getRestart ()) {

			taskLogger.noticeFormat (
				"Restarting console deployment %s",
				consoleDeployment.getServiceName ());

			restartService (
				consoleDeployment.getServiceName ());

			consoleDeployment

				.setRestart (
					false);

		}

	}

	private
	void restartService (
			@NonNull String serviceName) {

		try {

			DBusConnection dbus =
				DBusConnection.getConnection (
					DBusConnection.SYSTEM);

			SystemdManagerDbus systemd =
				SystemdManagerDbus.get (
					dbus);

			SystemdUnitDbus systemdUnit =
				systemd.getUnit (
					serviceName);

			systemdUnit.restart (
				"replace");

		} catch (DBusException dbusException) {

			throw new RuntimeException (
				dbusException);

		}

	}

	private
	DeploymentState getServiceState (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String serviceName) {

		TaskLogger taskLogger =
			logContext.nestTaskLoggerFormat (
				parentTaskLogger,
				"getServiceState (%s)",
				serviceName);

		try {

			DBusConnection dbus =
				DBusConnection.getConnection (
					DBusConnection.SYSTEM);

			SystemdManagerDbus systemd =
				SystemdManagerDbus.get (
					dbus);

			SystemdUnitDbus systemdUnit =
				systemd.getUnit (
					serviceName);

			String unitLoadState =
				systemdUnit.loadState ();

			String unitActiveState =
				systemdUnit.activeState ();

			String unitSubState =
				systemdUnit.subState ();

			if (
				stringNotEqualSafe (
					unitLoadState,
					"loaded")
			) {

				return DeploymentState.stopped;

			}

			switch (unitActiveState) {

			case "active":

				if (
					stringEqualSafe (
						unitSubState,
						"running")
				) {
					return DeploymentState.running;
				} else {

					taskLogger.warningFormat (
						"Service %s ",
						serviceName,
						"state is active but sub-state is %s",
						unitSubState);

					return DeploymentState.error;

				}

			case "inactive":

				return DeploymentState.stopped;

			case "activating":

				return DeploymentState.starting;

			case "deactivating":

				return DeploymentState.stopping;

			case "failed":

				return DeploymentState.error;

			default:

				taskLogger.warningFormat (
					"Service %s ",
					serviceName,
					"is in unknown state %s",
					unitActiveState);

				return DeploymentState.error;

			}

		} catch (DBusException dbusException) {

			throw new RuntimeException (
				dbusException);

		}

	}

}
