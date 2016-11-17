package wbs.framework.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import wbs.framework.component.manager.ComponentManager;
import wbs.framework.logging.DefaultLogContext;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

public
class BeanFilterProxy
	implements Filter {

	private final static
	LogContext logContext =
		DefaultLogContext.forClass (
			BeanFilterProxy.class);

	Filter target;

	@Override
	public
	void destroy () {
		target.destroy ();
	}

	@Override
	public
	void doFilter (
			ServletRequest servletRequest,
			ServletResponse servletResponse,
			FilterChain filterChain)
		throws
			IOException,
			ServletException {

		target.doFilter (
			servletRequest,
			servletResponse,
			filterChain);

	}

	@Override
	public
	void init (
			FilterConfig filterConfig)
		throws ServletException {

		TaskLogger taskLogger =
			logContext.createTaskLogger (
				"init");

		ServletContext servletContext =
			filterConfig.getServletContext ();

		ComponentManager componentManager =
			(ComponentManager)
			servletContext.getAttribute (
				"wbs-application-context");

		target =
			componentManager.getComponentRequired (
				taskLogger,
				filterConfig.getFilterName (),
				Filter.class);

		taskLogger.makeException ();

		target.init (
			filterConfig);

	}

}
