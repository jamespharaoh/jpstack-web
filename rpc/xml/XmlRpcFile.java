package wbs.platform.rpc.xml;

import java.io.IOException;

import javax.servlet.ServletException;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.api.mvc.WebApiAction;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.web.context.RequestContext;
import wbs.web.file.WebFile;
import wbs.web.responder.Responder;

@Accessors (fluent = true)
@PrototypeComponent ("xmlRpcFile")
public
class XmlRpcFile
	implements WebFile {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// properties

	@Getter @Setter
	WebApiAction action;

	// public implementation

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
	void doGet (
			@NonNull TaskLogger taskLogger)
		throws
			ServletException,
			IOException {

		doHeaders ();

	}

	@Override
	public
	void doOptions (
			@NonNull TaskLogger taskLogger)
		throws
			ServletException,
			IOException {

		doHeaders ();

	}

	@Override
	public
	void doPost (
			@NonNull TaskLogger parentTaskLogger)
		throws
			ServletException,
			IOException {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"doPost");

		doHeaders ();

		Responder responder =
			action.go (
				taskLogger);

		responder.execute (
			taskLogger);

	}

}
