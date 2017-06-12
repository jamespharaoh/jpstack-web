package wbs.console.misc;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringSplitNewline;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

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
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.io.RuntimeIoException;
import wbs.utils.string.FormatWriter;
import wbs.utils.string.WriterFormatWriter;

import wbs.web.mvc.WebExceptionHandler;

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

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"init");

		) {

			taskLogger.noticeFormat (
				"Initialised console exception handler");

		}

	}

	@Override
	public
	void handleExceptionRetry (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long attempt,
			@NonNull Throwable throwable) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"handleException");

		) {

			// log it via task logger

			taskLogger.warningFormatException (
				throwable,
				"Request generated exception on attempt %s, ",
				integerToDecimalString (
					attempt),
				"will retry: %s",
				requestContext.requestUri ());

			// log it in database

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
					GenericExceptionResolution.tryAgainNow);

			} catch (RuntimeException localException) {

				taskLogger.fatalFormatException (
					localException,
					"Error creating exception log");

			}

		}

	}

	@Override
	public
	void handleExceptionFinal (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long attempt,
			@NonNull Throwable throwable) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"handleExceptionFinal");

		) {

			// log it via task logger

			taskLogger.errorFormatException (
				throwable,
				"Request generated exception on attempt %s, ",
				integerToDecimalString (
					attempt),
				"giving up: %s",
				requestContext.requestUri ());

			// log it in database

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

			// reset output buffer if possible

			if (requestContext.canGetWriter ()) {

				if (! requestContext.isCommitted ()) {

					requestContext.reset ();

					errorPageProvider.get ()

						.exception (
							throwable)

						.execute (
							taskLogger);

				} else {

					try (

						Writer writer =
							requestContext.writer ();

						FormatWriter formatWriter =
							new WriterFormatWriter (
								writer);

					) {

						formatWriter.writeLineFormat (
							"<p class=\"error\">Internal error</p>");

						formatWriter.writeLineFormat (
							"<p>This page cannot be displayed properly, due ",
							"to an internal error.</p>");

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

					} catch (IOException ioExecption) {

						throw new RuntimeIoException (
							ioExecption);

					}

				}

			}

		}

	}

}
