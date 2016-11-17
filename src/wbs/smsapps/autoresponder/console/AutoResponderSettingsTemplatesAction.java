package wbs.smsapps.autoresponder.console;

import javax.servlet.ServletException;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.web.responder.Responder;

@PrototypeComponent ("autoResponderSettingsTemplatesAction")
public
class AutoResponderSettingsTemplatesAction
	extends ConsoleAction {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// public implementation

	@Override
	protected
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger)
		throws ServletException {

		@SuppressWarnings ("unused")
		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"goReal");

		requestContext.addNotice (
			"Action performed");

		return null;

	}

}
