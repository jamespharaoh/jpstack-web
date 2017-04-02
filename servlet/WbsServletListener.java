package wbs.platform.servlet;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringSplitComma;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

import lombok.NonNull;

import wbs.framework.component.manager.ComponentManager;
import wbs.framework.component.tools.ComponentManagerBuilder;
import wbs.framework.component.tools.ThreadLocalProxyComponentFactory;
import wbs.framework.logging.DefaultLogContext;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import info.faljse.SDNotify.SDNotify;
import wbs.web.context.RequestContextImplementation;

public
class WbsServletListener
	implements
		ServletContextListener,
		ServletRequestListener {

	// logging

	private final static
	LogContext logContext =
		DefaultLogContext.forClass (
			WbsServletListener.class);

	// state

	ServletContext servletContext;

	ComponentManager componentManager;

	Thread watchdogThread;

	// public implementation

	@Override
	public
	void contextInitialized (
			@NonNull ServletContextEvent event) {

		TaskLogger taskLogger =
			logContext.createTaskLogger (
				"contextInitialized");

		// setup components

		taskLogger.noticeFormat (
			"Initialising components");

		SDNotify.sendStatus (
			"Initialising components");

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

		List <String> layerNames =
			stringSplitComma (
				servletContext.getInitParameter (
					"layerNames"));

		componentManager =
			new ComponentManagerBuilder ()

			.primaryProjectName (
				primaryProjectName)

			.primaryProjectPackageName (
				primaryProjectPackageName)

			.layerNames (
				layerNames)

			.configNames (
				Collections.emptyList ())

			.outputPath (
				beanDefinitionOutputPath)

			.addSingletonComponent (
				"servletContext",
				event.getServletContext ())

			.build (
				taskLogger);

		servletContext.setAttribute (
			"wbs-application-context",
			componentManager);

		// systemd integration

		SDNotify.sendNotify ();

		watchdogThread =
			new Thread (
				this::watchdogThread);

		watchdogThread.start ();

		SDNotify.sendStatus (
			"Running");

	}

	@Override
	public
	void contextDestroyed (
			@NonNull ServletContextEvent event) {

		TaskLogger taskLogger =
			logContext.createTaskLogger (
				"contextDestroyed");

		// update systemd status

		SDNotify.sendStatus (
			"Shutting down");

		// clean up components

		if (
			isNotNull (
				componentManager)
		) {

			taskLogger.noticeFormat (
				"Destroying application context");
	
			componentManager.close ();

		}

		// systemd integration

		if (
			isNotNull (
				watchdogThread)
		) {

			taskLogger.noticeFormat (
				"Stopping systemd watchdog");

			watchdogThread.interrupt ();
	
			try {
	
				watchdogThread.wait ();
	
			} catch (InterruptedException interruptedException) {
	
				throw new RuntimeException (
					interruptedException);
	
			}

		}

	}

	@Override
	public
	void requestDestroyed (
			@NonNull ServletRequestEvent event) {

		TaskLogger taskLogger =
			logContext.createTaskLogger (
				"requestDestroyed");

		for (
			String requestBeanName
				: componentManager.requestComponentNames ()
		) {

			ThreadLocalProxyComponentFactory.Control control =
				genericCastUnchecked (
					componentManager.getComponentRequired (
						taskLogger,
						requestBeanName,
						Object.class));

			control.threadLocalProxyReset ();

		}

		RequestContextImplementation
			.servletRequestThreadLocal
			.remove ();

	}

	@Override
	public
	void requestInitialized (
			@NonNull ServletRequestEvent event) {

		TaskLogger taskLogger =
			logContext.createTaskLogger (
				"requestInitialized");

		boolean setServletContext = false;
		boolean setServletRequest = false;

		List <String> setRequestBeanNames =
			new ArrayList<> ();

		boolean success = false;

		try {

			RequestContextImplementation.servletContextThreadLocal.set (
				servletContext);

			setServletContext = true;

			RequestContextImplementation.servletRequestThreadLocal.set (
				(HttpServletRequest)
				event.getServletRequest ());

			setServletRequest = true;

			for (
				String requestBeanName
					: componentManager.requestComponentNames ()
			) {

				ThreadLocalProxyComponentFactory.Control control =
					genericCastUnchecked (
						componentManager.getComponentRequired (
							taskLogger,
							requestBeanName,
							Object.class));

				String targetBeanName =
					stringFormat (
						"%sTarget",
						requestBeanName);

				Object targetBean =
					componentManager.getComponentRequired (
						taskLogger,
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

				for (
					String requestBeanName
						: setRequestBeanNames
				) {

					ThreadLocalProxyComponentFactory.Control control =
						genericCastUnchecked (
							componentManager.getComponentRequired (
								taskLogger,
								requestBeanName,
								Object.class));

					control.threadLocalProxyReset ();

				}

				if (setServletRequest) {

					RequestContextImplementation
						.servletRequestThreadLocal
						.remove ();

				}

				if (setServletContext) {

					RequestContextImplementation
						.servletContextThreadLocal
						.remove ();

				}

			}

		}

	}

	// private implementation

	private
	void watchdogThread () {

		for (;;) {

			try {

				Thread.sleep (5000);

			} catch (InterruptedException exception) {

				return;

			}

			SDNotify.sendWatchdog ();

		}

	}

}
