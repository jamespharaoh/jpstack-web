package wbs.framework.web;

import javax.servlet.ServletException;

/**
 * A WebAction does something in response to a request and returns an
 * appropriate "Responder" to send the response.
 */
public
interface Action {

	Responder handle ()
		throws ServletException;

}
