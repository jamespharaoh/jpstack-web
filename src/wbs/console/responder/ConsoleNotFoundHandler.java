package wbs.console.responder;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletException;

import lombok.extern.log4j.Log4j;

import com.google.common.base.Optional;

import wbs.console.part.NotFoundPart;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.tab.Tab;
import wbs.console.tab.TabContext;
import wbs.console.tab.TabbedResponder;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.web.WebNotFoundHandler;
import wbs.platform.exception.logic.ExceptionLogger;
import wbs.platform.exception.model.ExceptionResolution;

@Log4j
@SingletonComponent ("notFoundHandler")
public
class ConsoleNotFoundHandler
	implements WebNotFoundHandler {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	ExceptionLogger exceptionLogger;

	@Inject
	Provider<NotFoundResponder> notFoundPage;

	@Inject
	Provider<NotFoundPart> notFoundPart;

	@Inject
	Provider<TabbedResponder> tabbedPage;

	// implementation

	private final
	Tab notFoundTab =
		new Tab ("Not found") {

		@Override
		public String getUrl () {

			return requestContext
				.requestPath ();

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
				Optional.fromNullable (
					requestContext.userId ()),
				ExceptionResolution.ignoreWithUserWarning);

		} catch (RuntimeException exception) {

			log.fatal (
				"Error creating not found log: " + exception.getMessage ());

		}

		// show the not found page

		TabContext tabContext =
			requestContext.tabContext ();

		if (tabContext != null) {

			tabbedPage.get ()
				.tab (notFoundTab)
				.title ("Page not found")
				.pagePart (notFoundPart.get ())
				.execute ();

		} else {

			notFoundPage.get ()
				.execute ();

		}

	}

}
