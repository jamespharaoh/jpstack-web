package wbs.platform.exception.console;

import static wbs.utils.thread.ConcurrentUtils.futureValue;

import java.util.concurrent.Future;

import javax.inject.Provider;

import com.google.gson.JsonObject;

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

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	NumExceptionsCache numExceptionsCache;

	@SingletonDependency
	NumFatalExceptionsCache numFatalExceptionsCache;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <ExceptionStatusLinePart> exceptionStatusLinePart;

	// details

	@Override
	public
	String typeName () {
		return "exceptions";
	}

	// implementation

	@Override
	public
	PagePart createPagePart (
			@NonNull TaskLogger parentTaskLogger) {

		return exceptionStatusLinePart.get ();

	}

	@Override
	public
	Future <JsonObject> getUpdateData (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull UserPrivChecker privChecker) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"getUpdateScript");

		JsonObject updateData =
			new JsonObject ();

		// count exceptions (if visible)

		if (
			privChecker.canRecursive (
				taskLogger,
				GlobalId.root,
				"alert")
		) {

			updateData.addProperty (
				"exceptions",
				numExceptionsCache.get (
					taskLogger));

		} else {

			updateData.addProperty (
				"exceptions",
				0);

		}

		updateData.addProperty (
			"fatalExceptions",
			numFatalExceptionsCache.get (
				taskLogger));

		return futureValue (
			updateData);

	}

}
