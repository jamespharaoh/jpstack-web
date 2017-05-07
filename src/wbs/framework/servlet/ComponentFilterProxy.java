package wbs.framework.servlet;

import static wbs.utils.etc.Misc.doNothing;
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

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.logging.BorrowedTaskLogger;
import wbs.framework.logging.CloseableTaskLogger;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.etc.ImplicitArgument.BorrowedArgument;
import wbs.utils.io.RuntimeIoException;

@PrototypeComponent ("componentFilterProxy")
public
class ComponentFilterProxy
	implements Filter {

	// singleton components

	@SingletonDependency
	ComponentManager componentManager;

	@ClassSingletonDependency
	LogContext logContext;

	// state

	FilterConfig filterConfig;

	FilterComponent target;

	// life cycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			target =
				componentManager.getComponentRequired (
					taskLogger,
					filterConfig.getFilterName (),
					FilterComponent.class);

			taskLogger.makeException ();

			target.setup (
				taskLogger,
				filterConfig);

		}

	}

	// implementation

	@Override
	public
	void init (
			@NonNull FilterConfig filterConfig)
		throws ServletException {

		this.filterConfig =
			filterConfig;

		ServletContext servletContext =
			filterConfig.getServletContext ();

		@SuppressWarnings ("resource")
		ComponentManager componentManager =
			genericCastUnchecked (
				servletContext.getAttribute (
					"wbs-application-context"));

		componentManager.bootstrapComponent (
			this);

	}

	@Override
	public
	void destroy () {

		doNothing ();

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

		try (

			BorrowedArgument <CloseableTaskLogger, BorrowedTaskLogger>
				parentTaskLogger =
					TaskLogger.implicitArgument.borrow ();

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger.get (),
					"doFilter");

		) {

			target.doFilter (
				taskLogger,
				servletRequest,
				servletResponse,
				new ComponentFilterChain () {

				@Override
				public
				void doFilter (
						@NonNull CloseableTaskLogger parentTaskLogger,
						@NonNull ServletRequest request,
						@NonNull ServletResponse response) {

					TaskLogger.implicitArgument.storeAndInvokeVoid (
						parentTaskLogger,
						() -> {

						try {

							filterChain.doFilter (
								request,
								response);

						} catch (ServletException servletException) {

							throw new RuntimeServletException (
								servletException);

						} catch (IOException ioException) {

							throw new RuntimeIoException (
								ioException);

						}

					});

				}

			});

		}

	}

}
