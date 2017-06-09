package wbs.web.responder;

import wbs.framework.logging.TaskLogger;

public
interface Responder {

	public
	void execute (
			TaskLogger parentTaskLogger);

}
