package wbs.framework.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import wbs.framework.application.context.ApplicationContext;

public
class BeanFilterProxy
	implements Filter {

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

		ServletContext servletContext =
			filterConfig.getServletContext ();

		ApplicationContext applicationContext =
			(ApplicationContext)
			servletContext.getAttribute ("wbs-application-context");

		target =
			applicationContext.getBean (
				filterConfig.getFilterName (),
				Filter.class);

		if (target == null)
			throw new RuntimeException ();

		target.init (
			filterConfig);

	}

}
