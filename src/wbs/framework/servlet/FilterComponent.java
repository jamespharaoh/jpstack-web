package wbs.framework.servlet;

import static wbs.utils.etc.Misc.doNothing;

import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import wbs.framework.logging.TaskLogger;

public
interface FilterComponent {

	default
	void setup (
			TaskLogger parentTaskLogger,
			FilterConfig filterConfig) {

		doNothing ();

	}

	void doFilter (
			TaskLogger parentTaskLogger,
			ServletRequest request,
			ServletResponse response,
			ComponentFilterChain chain);

}
