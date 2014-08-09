package wbs.platform.text.web;

import java.io.PrintWriter;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;

// TODO this belongs elsewhere

@Accessors (fluent = true)
@PrototypeComponent ("textResponder")
public
class TextResponder
	implements
		Provider<Responder>,
		Responder {

	@Inject
	RequestContext requestContext;

	@Getter @Setter
	String text;

	@Override
	public
	void execute () {

		PrintWriter out =
			requestContext.writer ();

		out.print (text);

	}

	@Override
	public
	Responder get () {

		return this;

	}

}
