package wbs.clients.apn.chat.user.admin.console;

import javax.inject.Provider;

import wbs.console.combo.ConsoleFormActionHelper;
import wbs.console.module.ConsoleManager;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.web.Responder;

public abstract
class AbstractConsoleFormActionHelper <FormState>
	implements ConsoleFormActionHelper <FormState> {

	// singleton dependencies

	@SingletonDependency
	ConsoleManager consoleManager;

	// utility methods

	protected
	Responder responder (
			String responderName) {

		Provider <Responder> responderProvider =
			consoleManager.responder (
				responderName,
				true);

		return responderProvider.get ();

	}

}
