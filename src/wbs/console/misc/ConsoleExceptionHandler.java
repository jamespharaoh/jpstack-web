package wbs.console.misc;

import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringSplitNewline;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;
import javax.servlet.ServletException;

import lombok.NonNull;

import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ErrorResponder;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.ExceptionUtils;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.utils.string.FormatWriter;

import wbs.web.handler.WebExceptionHandler;

@SingletonComponent ("exceptionHandler")
public
class ConsoleExceptionHandler
	implements WebExceptionHandler {

	// singleton dependencies

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@SingletonDependency
	ExceptionUtils exceptionLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	ConsoleRequestContext consoleRequestContext;

	@SingletonDependency
	ConsoleUserHelper consoleUserHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <ErrorResponder> errorPageProvider;

	// implementation

	@NormalLifecycleSetup
	public
	void init (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"init");

		taskLogger.noticeFormat (
			"Initialised console exception handler");

	}

	// TODO use this from other places? or what?
	@Override
	public
	void handleException (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Throwable throwable)
		throws
			ServletException,
			IOException {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"handleException");

		// log it the old fashioned way

		taskLogger.errorFormatException (
			throwable,
			"Request generated exception: %s",
			requestContext.requestUri ());

		// make an exception log of this calamity

		try {

			exceptionLogger.logThrowable (
				taskLogger,
				"console",
				stringFormat (
					"%s %s",
					requestContext.method (),
					requestContext.requestUri ()),
				throwable,
				consoleUserHelper.loggedInUserId (),
				GenericExceptionResolution.ignoreWithUserWarning);

		} catch (RuntimeException localException) {

			taskLogger.fatalFormatException (
				localException,
				"Error creating exception log");

		}

		// now if possible reset the output buffer

		if (requestContext.canGetWriter ()) {

			if (! requestContext.isCommitted ()) {

				requestContext.reset ();

				errorPageProvider.get ()

					.exception (
						throwable)

					.execute (
						taskLogger);

			} else {

				FormatWriter formatWriter =
					requestContext.formatWriter ();

				formatWriter.writeLineFormat (
					"<p class=\"error\">Internal error</p>");

				formatWriter.writeLineFormat (
					"<p>This page cannot be displayed properly, due to an ",
					"internal error.</p>");

				formatWriter.writeLineFormatIncreaseIndent (
					"<form method=\"%h\">",
					requestContext.method ());

				for (
					Map.Entry <String, List <String>> entry
						: requestContext.parameterMap ().entrySet ()
				) {

					String name =
						entry.getKey ();

					List<String> values =
						entry.getValue ();

					if (
						stringEqualSafe (
							name,
							"__repost")
					) {
						continue;
					}

					for (String value : values) {

						formatWriter.writeLineFormat (
							"<input",
							" type=\"hidden\"",
							" name=\"%h\"",
							name,
							" value=\"%h\"",
							value,
							">");
					}

					formatWriter.writeLineFormat (
						"<input",
						" type=\"submit\"",
						" name=\"__repost\"",
						" value=\"try again\"",
						">");

				}

				formatWriter.writeLineFormatDecreaseIndent (
					"</form>");

				if (
					privChecker.canSimple (
						taskLogger,
						GlobalId.root,
						"debug")
				) {

					formatWriter.writeLineFormatIncreaseIndent (
						"<pre>");

					List <String> exceptionDumpLines =
						stringSplitNewline (
							exceptionLogic.throwableDump (
								taskLogger,
								throwable));

					exceptionDumpLines.forEach (
						exceptionDumpLine ->
							formatWriter.writeLineFormat (
								"%s",
								exceptionDumpLine));

					formatWriter.writeLineFormatDecreaseIndent (
						"</pre>");

				}

			}

		}

	}

}
