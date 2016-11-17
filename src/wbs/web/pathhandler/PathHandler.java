package wbs.web.pathhandler;

import javax.servlet.ServletException;

import wbs.framework.logging.TaskLogger;
import wbs.web.file.WebFile;

public
interface PathHandler {

	public
	WebFile processPath (
			TaskLogger taskLogger,
			String path)
		throws ServletException;

}
