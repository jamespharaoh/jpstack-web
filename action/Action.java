package wbs.web.action;

import wbs.framework.logging.TaskLogger;
import wbs.web.responder.Responder;

/**
 * A WebAction does something in response to a request and returns an
 * appropriate "Responder" to send the response.
 */
public
interface Action {

	Responder handle (
			TaskLogger taskLogger);

}
