package wbs.imchat.core.api;

import javax.servlet.ServletException;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.web.Action;
import wbs.framework.web.Responder;

@PrototypeComponent ("imChatTestAction")
public 
class ImChatTestAction
	implements Action {

	@Override
	public 
	Responder handle ()
		throws ServletException {

		return null;

	}

}
