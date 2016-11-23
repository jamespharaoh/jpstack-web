package wbs.platform.exception.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.thread.ConcurrentUtils.futureValue;

import java.util.concurrent.Future;

import javax.inject.Provider;

import wbs.console.part.PagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;

import wbs.platform.status.console.StatusLine;

@SingletonComponent ("exceptionStatusLine")
public
class ExceptionStatusLine
	implements StatusLine {

	// singleton dependencies

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	NumExceptionsCache numExceptionsCache;

	@SingletonDependency
	NumFatalExceptionsCache numFatalExceptionsCache;

	@SingletonDependency
	UserPrivChecker privChecker;

	// prototype dependencies

	@PrototypeDependency
	Provider <ExceptionStatusLinePart> exceptionStatusLinePart;

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
				integerToDecimalString (
					numExceptions),
				integerToDecimalString (
					numExceptionsFatal)));

	}

}
