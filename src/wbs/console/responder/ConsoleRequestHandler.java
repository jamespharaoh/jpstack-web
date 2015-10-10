package wbs.console.responder;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.module.ConsoleManager;
import wbs.framework.web.RequestHandler;
import wbs.framework.web.Responder;

public abstract
class ConsoleRequestHandler
	implements RequestHandler {

	@Inject
	ConsoleManager consoleManager;

	public
	Provider<Responder> responder (
			String responderName) {

		return consoleManager.responder (
			responderName,
			true);

	}

}
