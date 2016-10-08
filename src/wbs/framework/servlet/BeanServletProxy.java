package wbs.framework.servlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import lombok.extern.log4j.Log4j;

import wbs.framework.component.manager.ComponentManager;
import wbs.framework.logging.TaskLogger;

@Log4j
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

		ComponentManager componentManager =
			(ComponentManager)
			servletContext.getAttribute (
				"wbs-application-context");

		TaskLogger taskLogger =
			new TaskLogger (
				log);

		target =
			componentManager.getComponentRequired (
				taskLogger,
				servletConfig.getServletName (),
				Servlet.class);

		taskLogger.makeException ();

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
