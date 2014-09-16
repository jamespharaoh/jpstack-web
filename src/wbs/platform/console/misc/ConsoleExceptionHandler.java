package wbs.platform.console.misc;

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
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.record.GlobalId;
import wbs.framework.utils.etc.StringFormatter;
import wbs.framework.web.WebExceptionHandler;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.console.responder.ErrorResponder;
import wbs.platform.exception.logic.ExceptionLogic;
import wbs.platform.exception.logic.ExceptionLogicImpl;
import wbs.platform.priv.console.PrivChecker;

@Log4j
@SingletonComponent ("exceptionHandler")
public
class ConsoleExceptionHandler
	implements WebExceptionHandler {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

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

			exceptionLogic.logThrowable (
				"console",
				stringFormat (
					"%s %s",
					requestContext.method (),
					requestContext.requestUri ()),
				throwable,
				requestContext.userId (),
				false);

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

				for (Map.Entry<String,List<String>> ent
						: requestContext.parameterMap ().entrySet ()) {

					String name =
						ent.getKey ();

					List<String> values =
						ent.getValue ();

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
						ExceptionLogicImpl.throwableDump (throwable));

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
