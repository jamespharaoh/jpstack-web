package wbs.console.responder;

import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOrEmptyString;
import static wbs.utils.string.StringUtils.stringFormat;

import java.io.IOException;

import javax.inject.Provider;
import javax.servlet.ServletException;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.misc.ConsoleUserHelper;
import wbs.console.part.NotFoundPart;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.tab.Tab;
import wbs.console.tab.TabContext;
import wbs.console.tab.TabbedResponder;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.web.handler.WebNotFoundHandler;

@SingletonComponent ("notFoundHandler")
public
class ConsoleNotFoundHandler
	implements WebNotFoundHandler {

	// singleton dependencies

	@SingletonDependency
	ConsoleUserHelper consoleUserHelper;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@ClassSingletonDependency
	LogContext logContext;

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
		String getUrl (
				@NonNull TaskLogger parentTaskLogger) {

			return requestContext.requestPath ();

		}

	};

	@Override
	public
	void handleNotFound (
			@NonNull TaskLogger parentTaskLogger)
		throws
			ServletException,
			IOException {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"handleNotFound");

		// log it the old fashioned way

		taskLogger.errorFormat (
			"Path not found: %s",
			requestContext.requestUri ());

		// make an exception log of this calamity

		try {

			String path =
				stringFormat (
					"%s%s",
					requestContext.servletPath (),
					optionalOrEmptyString (
						requestContext.pathInfo ()));

			exceptionLogger.logSimple (
				taskLogger,
				"console",
				path,
				"Not found",
				"The specified path was not found",
				consoleUserHelper.loggedInUserId (),
				GenericExceptionResolution.ignoreWithUserWarning);

		} catch (RuntimeException exception) {

			taskLogger.fatalFormatException (
				exception,
				"Error creating not found log: %s",
				 exception.getMessage ());

		}

		// show the not found page

		Optional <TabContext> tabContextOptional =
			requestContext.tabContext ();

		if (
			optionalIsPresent (
				tabContextOptional)
		) {

			tabbedPageProvider.get ()

				.tab (
					notFoundTab)

				.title (
					"Page not found")

				.pagePart (
					notFoundPartProvider.get ())

				.execute (
					taskLogger);

		} else {

			notFoundPageProvider.get ()

				.execute (
					taskLogger);

		}

	}

}
