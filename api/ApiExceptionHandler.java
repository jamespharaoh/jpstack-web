package wbs.platform.api;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.web.RequestContext;
import wbs.framework.web.WebExceptionHandler;
import wbs.platform.exception.logic.ExceptionLogic;
import wbs.platform.exception.logic.ExceptionLogicImpl;

@Log4j
@SingletonComponent ("exceptionHandler")
public
class ApiExceptionHandler
	implements WebExceptionHandler {

	@Inject
	ExceptionLogic exceptionLogic;

	@Inject
	RequestContext requestContext;

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
				"Request generated exception: %s: %s",
				requestContext.requestUri (),
				throwable.getMessage ()));

		// make an exception log of this calamity

		try {

			StringBuilder stringBuilder =
				new StringBuilder ();

			stringBuilder.append (
				ExceptionLogicImpl.throwableDump (throwable));

			stringBuilder.append (
				"\n\nHTTP INFO\n\n");

			stringBuilder.append (
				"METHOD = " + requestContext.method () + "\n\n");

			for (Map.Entry<String,List<String>> entry
					: requestContext.parameterMap ().entrySet ()) {

				for (String value : entry.getValue ()) {

					stringBuilder.append (
						entry.getKey () + " = \"" + value + "\"\n");

				}

			}

			exceptionLogic.logSimple (
				"webapi",
				requestContext.requestUri (),
				ExceptionLogicImpl.throwableSummary (throwable),
				stringBuilder.toString (),
				null,
				false);

		} catch (RuntimeException exception) {

			log.fatal (
				"Error creating exception log",
				exception);

		}

		// set the error code

		requestContext.status (
			HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

	}

}
