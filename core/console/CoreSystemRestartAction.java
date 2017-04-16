package wbs.platform.core.console;

import static wbs.utils.string.StringUtils.stringStartsWithSimple;
import static wbs.utils.string.StringUtils.substringFrom;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.deployment.console.ApiDeploymentConsoleHelper;
import wbs.platform.deployment.console.ConsoleDeploymentConsoleHelper;
import wbs.platform.deployment.console.DaemonDeploymentConsoleHelper;
import wbs.platform.deployment.model.ApiDeploymentRec;
import wbs.platform.deployment.model.ConsoleDeploymentRec;
import wbs.platform.deployment.model.DaemonDeploymentRec;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.web.responder.Responder;

@PrototypeComponent ("coreSystemRestartAction")
public
class CoreSystemRestartAction
	extends ConsoleAction {

	// singleton components

	@SingletonDependency
	ApiDeploymentConsoleHelper apiDeploymentHelper;

	@SingletonDependency
	ConsoleDeploymentConsoleHelper consoleDeploymentHelper;

	@SingletonDependency
	DaemonDeploymentConsoleHelper daemonDeploymentHelper;

	@SingletonDependency
	EventLogic eventLogic;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserPrivChecker userPrivChecker;

	// details

	@Override
	protected
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return responder (
			"coreSystemRestartResponder");

	}

	// implementation

	@Override
	protected
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"goReal");

		try (

			Transaction transaction =
				database.beginReadWrite (
					taskLogger,
					"goReal ()",
					this);

		) {

			for (
				String parameterName
					: requestContext.parameterMap ().keySet ()
			) {

				if (
					stringStartsWithSimple (
						"api/",
						parameterName)
				) {

					restartApi (
						taskLogger,
						transaction,
						substringFrom (
							parameterName,
							"api/".length ()));

				}


				if (
					stringStartsWithSimple (
						"console/",
						parameterName)
				) {

					restartConsole (
						taskLogger,
						transaction,
						substringFrom (
							parameterName,
							"console/".length ()));

				}


				if (
					stringStartsWithSimple (
						"daemon/",
						parameterName)
				) {

					restartDaemon (
						taskLogger,
						transaction,
						substringFrom (
							parameterName,
							"daemon/".length ()));

				}

			}

			return null;

		}

	}

	private
	void restartApi (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Transaction transaction,
			@NonNull String apiDeploymentCode) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"restartApi");

		ApiDeploymentRec apiDeployment =
			apiDeploymentHelper.findByCodeRequired (
				GlobalId.root,
				apiDeploymentCode);

		if (
			! userPrivChecker.canRecursive (
				taskLogger,
				apiDeployment,
				"restart")
		) {

			requestContext.addError (
				"Permission denied");

			return;

		}

		if (apiDeployment.getRestart ()) {

			requestContext.addWarning (
				"Already restarting");

			return;

		}

		apiDeployment

			.setRestart (
				true);

		eventLogic.createEvent (
			taskLogger,
			"api_deployment_restarted",
			userConsoleLogic.userRequired (),
			apiDeployment);

		transaction.commit ();

		requestContext.addNotice (
			"Restart triggered");

	}

	private
	void restartConsole (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Transaction transaction,
			@NonNull String consoleDeploymentCode) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"restartConsole");

		ConsoleDeploymentRec consoleDeployment =
			consoleDeploymentHelper.findByCodeRequired (
				GlobalId.root,
				consoleDeploymentCode);

		if (
			! userPrivChecker.canRecursive (
				taskLogger,
				consoleDeployment,
				"restart")
		) {

			requestContext.addError (
				"Permission denied");

			return;

		}

		if (consoleDeployment.getRestart ()) {

			requestContext.addWarning (
				"Already restarting");

			return;

		}

		consoleDeployment

			.setRestart (
				true);

		eventLogic.createEvent (
			taskLogger,
			"console_deployment_restarted",
			userConsoleLogic.userRequired (),
			consoleDeployment);

		transaction.commit ();

		requestContext.addNotice (
			"Restart triggered");

	}

	private
	void restartDaemon (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Transaction transaction,
			@NonNull String daemonDeploymentCode) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"restartDaemon");

		DaemonDeploymentRec daemonDeployment =
			daemonDeploymentHelper.findByCodeRequired (
				GlobalId.root,
				daemonDeploymentCode);

		if (
			! userPrivChecker.canRecursive (
				taskLogger,
				daemonDeployment,
				"restart")
		) {

			requestContext.addError (
				"Permission denied");

			return;

		}

		if (daemonDeployment.getRestart ()) {

			requestContext.addWarning (
				"Already restarting");

			return;

		}

		daemonDeployment

			.setRestart (
				true);

		eventLogic.createEvent (
			taskLogger,
			"daemon_deployment_restarted",
			userConsoleLogic.userRequired (),
			daemonDeployment);

		transaction.commit ();

		requestContext.addNotice (
			"Restart triggered");

	}

}
