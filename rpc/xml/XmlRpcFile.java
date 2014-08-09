package wbs.platform.rpc.xml;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.framework.web.WebFile;
import wbs.platform.api.WebApiAction;

@Accessors (fluent = true)
@PrototypeComponent ("xmlRpcFile")
public
class XmlRpcFile
	implements WebFile {

	@Inject
	RequestContext requestContext;

	@Getter @Setter
	WebApiAction action;

	public
	void doHeaders () {

		requestContext.setHeader (
			"Access-Control-Allow-Origin",
			"*");

		requestContext.setHeader (
			"Access-Control-Allow-Methods",
			"POST, OPTIONS");

		requestContext.setHeader (
			"Access-Control-Allow-Headers",
			"CONTENT-TYPE, X-REQUESTED-WITH");

		requestContext.setHeader (
			"Access-Control-Max-Age",
			"86400");

		requestContext.setHeader (
			"Allow",
			"POST, OPTIONS");

	}

	@Override
	public
	void doGet ()
		throws
			ServletException,
			IOException {

		doHeaders ();

	}

	@Override
	public
	void doOptions ()
		throws
			ServletException,
			IOException {

		doHeaders ();

	}

	@Override
	public
	void doPost ()
		throws
			ServletException,
			IOException {

		doHeaders ();

		Responder responder =
			action.go ();

		responder.execute ();

	}

}
