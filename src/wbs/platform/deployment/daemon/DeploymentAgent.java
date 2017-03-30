package wbs.platform.deployment.daemon;

import static wbs.utils.etc.DebugUtils.debugFormat;
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

		debugFormat (
			"Console deployment: %s",
			consoleDeployment.getName ());

		try {

			consoleDeployment

				.setState (
					getServiceState (
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

	}

	private
	DeploymentState getServiceState (
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
					return DeploymentState.error;
				}

			case "inactive":

				return DeploymentState.stopped;

			case "activating":

				return DeploymentState.starting;

			case "deactivating":

				return DeploymentState.stopping;

			default:

				return DeploymentState.error;

			}

		} catch (DBusException dbusException) {

			throw new RuntimeException (
				dbusException);

		}

	}

}
