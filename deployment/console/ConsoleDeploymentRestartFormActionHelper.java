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

import wbs.platform.deployment.model.ConsoleDeploymentRec;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.utils.string.FormatWriter;

import wbs.web.responder.Responder;

@PrototypeComponent ("consoleDeploymentRestartFormActionHelper")
public
class ConsoleDeploymentRestartFormActionHelper
	implements ConsoleFormActionHelper <Object> {

	// singleton dependencies

	@SingletonDependency
	ConsoleDeploymentConsoleHelper consoleDeploymentHelper;

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
	Pair <Boolean, Boolean> canBePerformed (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"canBePerformed");

		ConsoleDeploymentRec consoleDeployment =
			consoleDeploymentHelper.findFromContextRequired ();

		boolean show =
			userPrivChecker.canRecursive (
				taskLogger,
				consoleDeployment,
				"restart");

		boolean enable =
			! consoleDeployment.getRestart ();

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

		ConsoleDeploymentRec consoleDeployment =
			consoleDeploymentHelper.findFromContextRequired ();

		if (consoleDeployment.getRestart ()) {

			htmlParagraphWriteFormat (
				"There is already a restart scheduled for this console ",
				"deployment, but it has not yet taken place. If this message ",
				"persists, please contact support.");

		} else {

			htmlParagraphWriteFormat (
				"Trigger a restart for this console deployment");

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

		ConsoleDeploymentRec consoleDeployment =
			consoleDeploymentHelper.findFromContextRequired ();

		// check state

		if (consoleDeployment.getRestart ()) {

			requestContext.addNotice (
				"Restart already scheduled for this console deployment");

			return optionalAbsent ();

		}

		// perform update

		consoleDeployment

			.setRestart (
				true);

		eventLogic.createEvent (
			taskLogger,
			"console_deployment_restarted",
			userConsoleLogic.userRequired (),
			consoleDeployment);

		// commit and return

		transaction.commit ();

		requestContext.addNotice (
			"Console deployment restart triggered");

		return optionalAbsent ();

	}

}
