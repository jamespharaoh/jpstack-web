package wbs.platform.core.console;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.experimental.Accessors;

import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.web.Responder;

@Accessors (fluent = true)
@PrototypeComponent ("coreRedirectResponder")
public
class CoreRedirectResponder
	implements
		Provider<Responder>,
		Responder {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	// implementation

	@Override
	public
	void execute ()
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
