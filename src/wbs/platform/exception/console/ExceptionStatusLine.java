package wbs.platform.exception.console;

import static wbs.framework.utils.etc.ConcurrentUtils.futureValue;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.part.PagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.entity.record.GlobalId;
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
	UserPrivChecker privChecker;

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
	Future<String> getUpdateScript () {

		Long numExceptions = 0l;
		Long numExceptionsFatal = 0l;

		// count exceptions (if visible)

		if (
			privChecker.canRecursive (
				GlobalId.root,
				"alert")
		) {

			numExceptions =
				numExceptionsCache.get ();

			numExceptionsFatal =
				numFatalExceptionsCache.get ();

		} else {

			numExceptionsFatal =
				numFatalExceptionsCache.get ();

		}

		// return

		return futureValue (
			stringFormat (
				"updateExceptions (%s, %s);\n",
				numExceptions,
				numExceptionsFatal));

	}

}
