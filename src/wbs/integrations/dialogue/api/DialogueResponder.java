package wbs.integrations.dialogue.api;

import java.io.IOException;
import java.io.PrintWriter;

import lombok.NonNull;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;
import wbs.web.context.RequestContext;
import wbs.web.responder.Responder;

@PrototypeComponent ("dialogueResponder")
public
class DialogueResponder
	implements Responder {

	// singleton dependencies

	@SingletonDependency
	RequestContext requestContext;

	// implementation

	@Override
	public
	void execute (
			@NonNull TaskLogger parentTaskLogger)
		throws IOException {

		PrintWriter out =
			requestContext.writer ();

		out.println ("<HTML>");
		out.println ("<!-- X-E3-Submission-Report: \"00\" -->");
		out.println ("</HTML>");

	}

}
