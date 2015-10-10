package wbs.platform.servlet;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.log4j.Log4j;
import wbs.framework.application.context.ApplicationContext;
import wbs.framework.application.tools.ApplicationContextBuilder;
import wbs.framework.application.tools.ThreadLocalProxyBeanFactory;
import wbs.framework.web.RequestContextImpl;

@Log4j
public
class WbsServletListener
	implements
		ServletContextListener,
		ServletRequestListener {

	ServletContext servletContext;

	ApplicationContext applicationContext;

	@Override
	public
	void contextDestroyed (
			ServletContextEvent event) {

		if (applicationContext == null)
			return;

		log.info (
			stringFormat (
				"Destroying application context"));

		applicationContext.close ();

	}

	@Override
	public
	void contextInitialized (
			ServletContextEvent event) {

		log.info (
			stringFormat (
				"Initialising application context"));

		servletContext =
			event.getServletContext ();

		String primaryProjectName =
			servletContext.getInitParameter (
				"primaryProjectName");

		String primaryProjectPackageName =
			servletContext.getInitParameter (
				"primaryProjectPackageName");

		String beanDefinitionOutputPath =
			servletContext.getInitParameter (
				"beanDefinitionOutputPath");

		List<String> layerNames =
			Arrays.asList (
				servletContext
					.getInitParameter ("layerNames")
					.split (","));

		List<String> configNames =
			Arrays.asList (
				servletContext
					.getInitParameter ("configNames")
					.split (","));

		applicationContext =
			new ApplicationContextBuilder ()

			.primaryProjectName (
				primaryProjectName)

			.primaryProjectPackageName (
				primaryProjectPackageName)

			.layerNames (
				layerNames)

			.configNames (
				configNames)

			.outputPath (
				beanDefinitionOutputPath)

			.addSingletonBean (
				"servletContext",
				event.getServletContext ())

			.build ();

		servletContext.setAttribute (
			"wbs-application-context",
			applicationContext);

	}

	@Override
	public
	void requestDestroyed (
			ServletRequestEvent event) {

		for (String requestBeanName
				: applicationContext.requestBeanNames ()) {

			ThreadLocalProxyBeanFactory.Control control =
				(ThreadLocalProxyBeanFactory.Control)
				applicationContext.getBean (
					requestBeanName,
					Object.class);

			control.threadLocalProxyReset ();

		}

		RequestContextImpl
			.servletRequestThreadLocal
			.remove ();

	}

	@Override
	public
	void requestInitialized (
			ServletRequestEvent event) {

		boolean setServletContext = false;
		boolean setServletRequest = false;

		List<String> setRequestBeanNames =
			new ArrayList<String> ();

		boolean success = false;

		try {

			RequestContextImpl.servletContextThreadLocal.set (
				servletContext);

			setServletContext = true;

			RequestContextImpl.servletRequestThreadLocal.set (
				(HttpServletRequest)
				event.getServletRequest ());

			setServletRequest = true;

			for (String requestBeanName
					: applicationContext.requestBeanNames ()) {

				ThreadLocalProxyBeanFactory.Control control =
					(ThreadLocalProxyBeanFactory.Control)
					applicationContext.getBean (
						requestBeanName,
						Object.class);

				String targetBeanName =
					stringFormat (
						"%sTarget",
						requestBeanName);

				Object targetBean =
					applicationContext.getBean (
						targetBeanName,
						Object.class);

				control.threadLocalProxySet (
					targetBean);

				setRequestBeanNames.add (
					requestBeanName);

			}

			success = true;

		} finally {

			if (! success) {

				for (String requestBeanName
						: setRequestBeanNames) {

					ThreadLocalProxyBeanFactory.Control control =
						(ThreadLocalProxyBeanFactory.Control)
						applicationContext.getBean (
							requestBeanName,
							Object.class);

					control.threadLocalProxyReset ();

				}

				if (setServletRequest) {

					RequestContextImpl
						.servletRequestThreadLocal
						.remove ();

				}

				if (setServletContext) {

					RequestContextImpl
						.servletContextThreadLocal
						.remove ();

				}

			}

		}

	}

}
