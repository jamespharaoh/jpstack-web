package wbs.platform.exception.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.part.PagePart;
import wbs.console.priv.PrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.record.GlobalId;
import wbs.platform.status.console.StatusLine;

@SingletonComponent ("exceptionStatusLine")
public
class ExceptionStatusLine
	implements StatusLine {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	NumExceptionsCache numExceptionsCache;

	@Inject
	NumFatalExceptionsCache numFatalExceptionsCache;

	@Inject
	PrivChecker privChecker;

	@Inject
	Provider<ExceptionStatusLinePart> exceptionStatusLinePart;

	// details

	@Override
	public
	String getName () {
		return "exceptions";
	}

	// implementation

	@Override
	public
	PagePart get () {

		return exceptionStatusLinePart.get ();

	}

	@Override
	public
	String getUpdateScript () {

		int numExceptions = 0;
		int numExceptionsFatal = 0;

		// count exceptions (if visible)

		if (privChecker.can (
				GlobalId.root,
				"alert")) {

			numExceptions =
				numExceptionsCache.get ();

			numExceptionsFatal =
				numFatalExceptionsCache.get ();

		} else {

			numExceptionsFatal =
				numFatalExceptionsCache.get ();

		}

		// return

		return stringFormat (
			"updateExceptions (%s, %s);\n",
			numExceptions,
			numExceptionsFatal);

	}

}
