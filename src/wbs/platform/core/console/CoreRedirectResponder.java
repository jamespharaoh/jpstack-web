package wbs.platform.core.console;

import java.io.IOException;

import javax.inject.Provider;

import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;
import wbs.web.responder.Responder;

@Accessors (fluent = true)
@PrototypeComponent ("coreRedirectResponder")
public
class CoreRedirectResponder
	implements
		Provider <Responder>,
		Responder {

	// singleton dependencies

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// implementation

	@Override
	public
	void execute (
			@NonNull TaskLogger parentTaskLogger)
		throws IOException {

		requestContext.sendRedirect (
			requestContext.applicationPathPrefix ());

	}

	@Override
	public
	Responder get () {

		return this;

	}

}
