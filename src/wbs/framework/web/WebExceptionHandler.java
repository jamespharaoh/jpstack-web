package wbs.framework.web;

import java.io.IOException;

import javax.servlet.ServletException;

public
interface WebExceptionHandler {

	void handleException (
			Throwable exception)
		throws
			ServletException,
			IOException;

}
