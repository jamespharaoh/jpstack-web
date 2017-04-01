package wbs.platform.deployment.console;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;

import wbs.console.formaction.ConsoleFormActionHelper;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.deployment.model.DaemonDeploymentRec;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.utils.string.FormatWriter;

import wbs.web.responder.Responder;

@PrototypeComponent ("daemonDeploymentRestartFormActionHelper")
public
class DaemonDeploymentRestartFormActionHelper
	implements ConsoleFormActionHelper <Object> {

	// singleton dependencies

	@SingletonDependency
	DaemonDeploymentConsoleHelper daemonDeploymentHelper;

	@SingletonDependency
	EventLogic eventLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserPrivChecker userPrivChecker;

	// public implementation

	@Override
	public
	Pair <Boolean, Boolean> canBePerformed () {

		DaemonDeploymentRec daemonDeployment =
			daemonDeploymentHelper.findFromContextRequired ();

		boolean show =
			userPrivChecker.canRecursive (
				daemonDeployment,
				"restart");

		boolean enable =
			! daemonDeployment.getRestart ();

		return Pair.of (
			show,
			enable);

	}

	@Override
	public
	void writePreamble (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter formatWriter,
			@NonNull Boolean submit) {

		DaemonDeploymentRec daemonDeployment =
			daemonDeploymentHelper.findFromContextRequired ();

		if (daemonDeployment.getRestart ()) {

			htmlParagraphWriteFormat (
				"There is already a restart scheduled for this daemon ",
				"deployment, but it has not yet taken place. If this message ",
				"persists, please contact support.");

		} else {

			htmlParagraphWriteFormat (
				"Trigger a restart for this daemon deployment");

		}

	}

	@Override
	public
	Optional <Responder> processFormSubmission (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Transaction transaction,
			@NonNull Object formState) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"processFormSubmission");

		// load data

		DaemonDeploymentRec daemonDeployment =
			daemonDeploymentHelper.findFromContextRequired ();

		// check state

		if (daemonDeployment.getRestart ()) {

			requestContext.addNotice (
				"Restart already scheduled for this daemon deployment");

			return optionalAbsent ();

		}

		// perform update

		daemonDeployment

			.setRestart (
				true);

		eventLogic.createEvent (
			taskLogger,
			"daemon_deployment_restarted",
			userConsoleLogic.userRequired (),
			daemonDeployment);

		// commit and return

		transaction.commit ();

		requestContext.addNotice (
			"Daemon deployment restart triggered");

		return optionalAbsent ();

	}

}
