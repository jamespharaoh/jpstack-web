package wbs.framework.servlet;

import javax.servlet.ServletException;

import lombok.NonNull;

public
class RuntimeServletException
	extends RuntimeException {

	public
	RuntimeServletException (
			@NonNull ServletException servletException) {

		super (
			servletException);

	}

}
