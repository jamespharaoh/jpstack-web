package wbs.framework.web;

import java.io.IOException;

import javax.servlet.ServletException;

/**
 * Abstraction to handle GET/POST requests for a single "file".
 *
 * The same WebFile can easily be reused for any number of different paths
 * etc... so the name here might be slightly misleading.
 */
public
interface WebFile {

	void doGet ()
		throws
			ServletException,
			IOException;

	void doPost ()
		throws
			ServletException,
			IOException;

	void doOptions ()
		throws
			ServletException,
			IOException;

}
