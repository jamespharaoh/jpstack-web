package wbs.web.action;

import wbs.framework.logging.TaskLogger;

import wbs.web.responder.Responder;

public
interface Action {

	Responder handle (
			TaskLogger parentTaskLogger);

}
