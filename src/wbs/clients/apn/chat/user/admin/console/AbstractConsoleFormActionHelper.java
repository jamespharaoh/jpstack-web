package wbs.clients.apn.chat.user.admin.console;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.web.Responder;
import wbs.platform.console.module.ConsoleManager;

public abstract
class AbstractConsoleFormActionHelper<FormState>
	implements ConsoleFormActionHelper<FormState> {

	// dependencies

	@Inject
	ConsoleManager consoleManager;

	// utility methods

	protected
	Responder responder (
			String responderName) {

		Provider<Responder> responderProvider =
			consoleManager.responder (
				responderName,
				true);

		return responderProvider.get ();

	}

}
