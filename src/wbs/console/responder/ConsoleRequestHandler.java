package wbs.console.responder;

import javax.inject.Provider;

import wbs.console.module.ConsoleManager;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.web.RequestHandler;
import wbs.framework.web.Responder;

public abstract
class ConsoleRequestHandler
	implements RequestHandler {

	// singleton dependencies

	@SingletonDependency
	ConsoleManager consoleManager;

	// implementation

	public
	Provider <Responder> responder (
			String responderName) {

		return consoleManager.responder (
			responderName,
			true);

	}

}
