package wbs.console.misc;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletException;

import lombok.extern.log4j.Log4j;

import com.google.common.base.Optional;

import wbs.console.priv.PrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ErrorResponder;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.ExceptionLogger.Resolution;
import wbs.framework.exception.ExceptionLogic;
import wbs.framework.record.GlobalId;
import wbs.framework.utils.etc.StringFormatter;
import wbs.framework.web.WebExceptionHandler;

@Log4j
@SingletonComponent ("exceptionHandler")
public
class ConsoleExceptionHandler
	implements WebExceptionHandler {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	ExceptionLogger exceptionLogger;

	@Inject
	ExceptionLogic exceptionLogic;

	@Inject
	PrivChecker privChecker;

	@Inject
	Provider<ErrorResponder> errorPage;

	@Inject
	ConsoleRequestContext consoleRequestContext;

	// state

	PrintWriter out;

	// implementation

	@PostConstruct
	public
	void init () {

		log.info (
			stringFormat (
				"Initialised console exception handler"));

	}

	// TODO use this from other places? or what?
	@Override
	public
	void handleException (
			Throwable throwable)
		throws
			ServletException,
			IOException {

		// log it the old fashioned way

		log.error (
			stringFormat (
				"Request generated exception: %s",
				requestContext.requestUri ()),
			throwable);

		// make an exception log of this calamity

		try {

			exceptionLogger.logThrowable (
				"console",
				stringFormat (
					"%s %s",
					requestContext.method (),
					requestContext.requestUri ()),
				throwable,
				Optional.fromNullable (
					requestContext.userId ()),
				Resolution.ignoreWithUserWarning);

		} catch (RuntimeException localException) {

			log.fatal (
				"Error creating exception log",
				localException);

		}

		// now if possible reset the output buffer

		if (requestContext.canGetWriter ()) {

			if (! requestContext.isCommitted ()) {

				requestContext.reset ();

				errorPage.get ()
					.exception (throwable)
					.execute ();

			} else {

				out =
					requestContext.writer ();

				printFormat (
					"<p class=\"error\">Internal error</p>\n");

				printFormat (
					"<p>This page cannot be displayed properly, due to an ",
					"internal error.</p>\n");

				printFormat (
					"<form method=\"%h\">\n",
					requestContext.method ());

				for (
					Map.Entry<String,List<String>> entry
						: requestContext.parameterMap ().entrySet ()
				) {

					String name =
						entry.getKey ();

					List<String> values =
						entry.getValue ();

					if (equal (name, "__repost"))
						continue;

					for (String value : values) {

						printFormat (
							"<input",
							" type=\"hidden\"",
							" name=\"%h\"",
							name,
							" value=\"%h\"",
							value,
							">\n");
					}

					printFormat (
						"<input",
						" type=\"submit\"",
						" name=\"__repost\"",
						" value=\"try again\"",
						">\n");

				}

				printFormat (
					"</form>\n");

				if (privChecker.can (
						GlobalId.root,
						"debug")) {

					printFormat (
						"<p><pre>%h</pre></p>\n",
						exceptionLogic.throwableDump (
							throwable));

				}

			}

		}

	}

	protected
	void printFormat (
			Object... args) {

		out.print (
			StringFormatter.standard (
				args));

	}

}
