package wbs.framework.servlet;

import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import lombok.NonNull;

import wbs.framework.component.manager.ComponentManager;
import wbs.framework.logging.DefaultLogContext;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;

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
			@NonNull FilterConfig filterConfig)
		throws ServletException {

		try (

			OwnedTaskLogger taskLogger =
				logContext.createTaskLogger (
					"init");

		) {

			ServletContext servletContext =
				filterConfig.getServletContext ();

			@SuppressWarnings ("resource")
			ComponentManager componentManager =
				genericCastUnchecked (
					servletContext.getAttribute (
						"wbs-application-context"));

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

}
