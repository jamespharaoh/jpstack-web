package wbs.test.simulator.console;

import java.io.IOException;
import java.io.PrintWriter;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.json.simple.JSONValue;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;

@Accessors (fluent = true)
@PrototypeComponent ("jsonResponder")
public
class JsonResponder
	implements
		Provider<Responder>,
		Responder {

	// dependencies

	@Inject
	RequestContext requestContext;

	// properties

	@Getter @Setter
	Object value;

	// implementation

	@Override
	public
	void execute ()
		throws IOException {

		requestContext.setHeader (
			"Content-Type",
			"application/json");

		PrintWriter out =
			requestContext.writer ();

		JSONValue.writeJSONString (
			value,
			out);

	}

	@Override
	public
	Responder get () {

		return this;

	}

}
