package wbs.web.responder;

import static wbs.utils.etc.Misc.doNothing;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.web.context.RequestContext;

public
class AbstractResponder
	implements Responder {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// implementation

	protected
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		doNothing ();

	}

	protected
	void tearDown (
			@NonNull TaskLogger parentTaskLogger) {

		doNothing ();

	}

	protected
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		doNothing ();

	}

	protected
	void goHeaders (
			@NonNull TaskLogger parentTaskLogger) {

		doNothing ();

	}

	protected
	void goContent (
			@NonNull TaskLogger parentTaskLogger) {

		doNothing ();

	}

	@Override
	public final
	void execute (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"execute");

		try (

			Transaction transaction =
				database.beginReadOnly (
					taskLogger,
					"AbstractResponder.execute ()",
					this);

		) {

			setup (
				taskLogger);

			try {

				prepare (
					taskLogger);

				goHeaders (
					taskLogger);

				goContent (
					taskLogger);

			} finally {

				tearDown (
					taskLogger);

			}

		}

	}

}
