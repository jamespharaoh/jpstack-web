package wbs.console.responder;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.io.IOException;

import javax.inject.Provider;
import javax.servlet.ServletException;

import lombok.extern.log4j.Log4j;

import wbs.console.misc.ConsoleUserHelper;
import wbs.console.part.NotFoundPart;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.tab.Tab;
import wbs.console.tab.TabContext;
import wbs.console.tab.TabbedResponder;
import wbs.framework.application.annotations.PrototypeDependency;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.web.WebNotFoundHandler;

@Log4j
@SingletonComponent ("notFoundHandler")
public
class ConsoleNotFoundHandler
	implements WebNotFoundHandler {

	// singleton dependencies

	@SingletonDependency
	ConsoleUserHelper consoleUserHelper;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <TabbedResponder> tabbedPageProvider;

	@PrototypeDependency
	Provider <NotFoundResponder> notFoundPageProvider;

	@PrototypeDependency
	Provider <NotFoundPart> notFoundPartProvider;

	// implementation

	private final
	Tab notFoundTab =
		new Tab ("Not found") {

		@Override
		public
		String getUrl () {

			return requestContext.requestPath ();

		}

	};

	@Override
	public
	void handleNotFound ()
		throws
			ServletException,
			IOException {

		// log it the old fashioned way

		log.error (
			"Path not found: " + requestContext.requestUri ());

		// make an exception log of this calamity

		try {

			String path =
				stringFormat (
					"%s%s",
					requestContext.servletPath (),
					requestContext.pathInfo () != null
						? requestContext.pathInfo ()
						: "");

			exceptionLogger.logSimple (
				"console",
				path,
				"Not found",
				"The specified path was not found",
				consoleUserHelper.loggedInUserId (),
				GenericExceptionResolution.ignoreWithUserWarning);

		} catch (RuntimeException exception) {

			log.fatal (
				"Error creating not found log: " + exception.getMessage ());

		}

		// show the not found page

		TabContext tabContext =
			requestContext.tabContext ();

		if (tabContext != null) {

			tabbedPageProvider.get ()
				.tab (notFoundTab)
				.title ("Page not found")
				.pagePart (notFoundPartProvider.get ())
				.execute ();

		} else {

			notFoundPageProvider.get ()
				.execute ();

		}

	}

}
