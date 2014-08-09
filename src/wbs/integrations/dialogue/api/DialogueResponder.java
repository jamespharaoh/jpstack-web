package wbs.integrations.dialogue.api;

import java.io.IOException;
import java.io.PrintWriter;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;

@PrototypeComponent ("dialogueResponder")
public
class DialogueResponder
	implements Responder {

	@Inject
	RequestContext requestContext;

	@Override
	public
	void execute ()
		throws IOException {

		PrintWriter out =
			requestContext.writer ();

		out.println ("<HTML>");
		out.println ("<!-- X-E3-Submission-Report: \"00\" -->");
		out.println ("</HTML>");

	}

}
