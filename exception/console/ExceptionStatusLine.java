package wbs.platform.exception.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.thread.ConcurrentUtils.futureValue;

import java.util.concurrent.Future;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.part.PagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.status.console.StatusLine;

@SingletonComponent ("exceptionStatusLine")
public
class ExceptionStatusLine
	implements StatusLine {

	// singleton dependencies

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@ClassSingletonDependency
	LogContext logContext;

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
	PagePart get (
			@NonNull TaskLogger parentTaskLogger) {

		return exceptionStatusLinePart.get ();

	}

	@Override
	public
	Future <String> getUpdateScript (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"getUpdateScript");

		Long numExceptions = 0l;
		Long numExceptionsFatal = 0l;

		// count exceptions (if visible)

		if (
			privChecker.canRecursive (
				taskLogger,
				GlobalId.root,
				"alert")
		) {

			numExceptions =
				numExceptionsCache.get (
					taskLogger);

			numExceptionsFatal =
				numFatalExceptionsCache.get (
					taskLogger);

		} else {

			numExceptionsFatal =
				numFatalExceptionsCache.get (
					taskLogger);

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
