package wbs.web.file;

import java.io.IOException;

import javax.servlet.ServletException;

import wbs.framework.logging.TaskLogger;

/**
 * Abstraction to handle GET/POST requests for a single "file".
 *
 * The same WebFile can easily be reused for any number of different paths
 * etc... so the name here might be slightly misleading.
 */
public
interface WebFile {

	void doGet (
			TaskLogger taskLogger)
		throws
			ServletException,
			IOException;

	void doPost (
			TaskLogger taskLogger)
		throws
			ServletException,
			IOException;

	void doOptions (
			TaskLogger taskLogger)
		throws
			ServletException,
			IOException;

}
