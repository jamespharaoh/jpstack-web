package wbs.platform.text.web;

import static wbs.utils.string.StringUtils.stringFormat;

import java.io.PrintWriter;

import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;

// TODO this belongs elsewhere

@Accessors (fluent = true)
@PrototypeComponent ("textResponder")
public
class TextResponder
	implements
		Provider <Responder>,
		Responder {

	@SingletonDependency
	RequestContext requestContext;

	@Getter @Setter
	String text;

	@Getter @Setter
	String contentType =
		"text/plain";

	@Override
	public
	void execute () {

		requestContext.setHeader (
			"Content-Type",
			stringFormat (
				"%s; charset=utf-8",
				contentType));

		PrintWriter out =
			requestContext.writer ();

		out.print (
			text);

	}

	@Override
	public
	Responder get () {

		return this;

	}

}
