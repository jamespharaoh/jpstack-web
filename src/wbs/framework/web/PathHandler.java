package wbs.framework.web;

import javax.servlet.ServletException;

public
interface PathHandler {

	public
	WebFile processPath (
			String path)
		throws ServletException;

}
