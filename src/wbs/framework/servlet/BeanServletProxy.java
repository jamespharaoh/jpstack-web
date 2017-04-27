package wbs.framework.servlet;

import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import lombok.NonNull;

import wbs.framework.component.manager.ComponentManager;
import wbs.framework.logging.DefaultLogContext;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

public
class BeanServletProxy
	implements Servlet {

	private final static
	LogContext logContext =
		DefaultLogContext.forClass (
			BeanServletProxy.class);

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
			@NonNull ServletConfig servletConfig)
		throws ServletException {

		try (

			TaskLogger taskLogger =
				logContext.createTaskLogger (
					"init");

		) {

			ServletContext servletContext =
				servletConfig.getServletContext ();

			@SuppressWarnings ("resource")
			ComponentManager componentManager =
				genericCastUnchecked (
					servletContext.getAttribute (
						"wbs-application-context"));

			target =
				componentManager.getComponentRequired (
					taskLogger,
					servletConfig.getServletName (),
					Servlet.class);

			taskLogger.makeException ();

			target.init (
				servletConfig);

		}

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
