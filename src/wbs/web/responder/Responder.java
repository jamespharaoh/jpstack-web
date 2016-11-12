package wbs.web.responder;

import java.io.IOException;

import wbs.framework.logging.TaskLogger;

public
interface Responder {

	public
	void execute (
			TaskLogger taskLogger)
		throws IOException;

}
