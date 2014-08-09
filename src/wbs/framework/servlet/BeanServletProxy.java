package wbs.framework.servlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import wbs.framework.application.context.ApplicationContext;

public
class BeanServletProxy
	implements Servlet {

	// state

	Servlet target;

	// implementation

	@Override
	public
	ServletConfig getServletConfig () {

		return target.getServletConfig ();

	}

	@Override
	public
	String getServletInfo () {

		return target.getServletInfo ();

	}

	@Override
	public
	void init (
			ServletConfig servletConfig)
		throws ServletException {

		ServletContext servletContext =
			servletConfig.getServletContext ();

		ApplicationContext applicationContext =
			(ApplicationContext)
			servletContext.getAttribute (
				"wbs-application-context");

		target =
			applicationContext.getBean (
				servletConfig.getServletName (),
				Servlet.class);

		if (target == null)
			throw new RuntimeException ();

		target.init (
			servletConfig);

	}

	@Override
	public
	void destroy () {
	}

	@Override
	public
	void service (
			ServletRequest servletRequest,
			ServletResponse servletResponse)
		throws
			ServletException,
			IOException {

		target.service (
			servletRequest,
			servletResponse);

	}

}
