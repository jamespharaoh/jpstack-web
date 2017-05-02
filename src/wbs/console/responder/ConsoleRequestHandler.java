package wbs.console.responder;

import javax.inject.Provider;

import wbs.console.module.ConsoleManager;

import wbs.framework.component.annotations.SingletonDependency;

import wbs.web.handler.RequestHandler;
import wbs.web.responder.Responder;

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
