package wbs.platform.deployment.console;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.formaction.ConsoleFormActionHelper;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.deployment.model.ApiDeploymentRec;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.utils.string.FormatWriter;

import wbs.web.responder.Responder;

@PrototypeComponent ("apiDeploymentRestartFormActionHelper")
public
class ApiDeploymentRestartFormActionHelper
	implements ConsoleFormActionHelper <Object, Object> {

	// singleton dependencies

	@SingletonDependency
	ApiDeploymentConsoleHelper apiDeploymentHelper;

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
	Permissions canBePerformed (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"canBePerformed");

		ApiDeploymentRec apiDeployment =
			apiDeploymentHelper.findFromContextRequired ();

		boolean show =
			userPrivChecker.canRecursive (
				taskLogger,
				apiDeployment,
				"restart");

		boolean enable =
			! apiDeployment.getRestart ();

		return new Permissions ()
			.canView (show)
			.canPerform (enable);

	}

	@Override
	public
	void writePreamble (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter formatWriter,
			@NonNull Boolean submit) {

		ApiDeploymentRec apiDeployment =
			apiDeploymentHelper.findFromContextRequired ();

		if (apiDeployment.getRestart ()) {

			htmlParagraphWriteFormat (
				"There is already a restart scheduled for this API ",
				"deployment, but it has not yet taken place. If this message ",
				"persists, please contact support.");

		} else {

			htmlParagraphWriteFormat (
				"Trigger a restart for this API deployment");

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

		ApiDeploymentRec apiDeployment =
			apiDeploymentHelper.findFromContextRequired ();

		// check state

		if (apiDeployment.getRestart ()) {

			requestContext.addNotice (
				"Restart already scheduled for this API deployment");

			return optionalAbsent ();

		}

		// perform update

		apiDeployment

			.setRestart (
				true);

		eventLogic.createEvent (
			taskLogger,
			"api_deployment_restarted",
			userConsoleLogic.userRequired (),
			apiDeployment);

		// commit and return

		transaction.commit ();

		requestContext.addNotice (
			"API deployment restart triggered");

		return optionalAbsent ();

	}

}
