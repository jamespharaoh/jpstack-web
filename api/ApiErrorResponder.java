package wbs.platform.api;

import java.io.PrintWriter;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;

@PrototypeComponent ("prototype")
public
class ApiErrorResponder
	implements Responder {

	@Inject
	RequestContext requestContext;

	@Override
	public
	void execute () {

		PrintWriter out =
			requestContext.writer ();

		out.println (
			"ERROR\nInternal error\n");

	}

}
