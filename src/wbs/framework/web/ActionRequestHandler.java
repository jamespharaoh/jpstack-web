package wbs.framework.web;

import java.io.IOException;

import javax.servlet.ServletException;

public
class ActionRequestHandler
	implements RequestHandler {

	Action action;

	public
	ActionRequestHandler (
			Action newAction) {

		action =
			newAction;

	}

	@Override
	public
	void handle ()
		throws
			ServletException,
			IOException {

		Responder responder =
			action.go ();

		if (responder == null)
			throw new NullPointerException ();

		responder.execute ();

	}

}
