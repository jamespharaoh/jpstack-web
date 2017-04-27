package wbs.console.responder;

import static wbs.utils.etc.Misc.doNothing;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.web.responder.Responder;

public abstract
class ConsoleResponder
	implements Responder {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	// state

	protected
	OwnedTransaction transaction;

	// implementation

	protected
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		doNothing ();

	}

	protected
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		doNothing ();

	}

	protected
	void setHtmlHeaders (
			@NonNull TaskLogger parentTaskLogger) {

		doNothing ();

	}

	protected
	void render (
			@NonNull TaskLogger parentTaskLogger) {

		doNothing ();

	}

	protected
	void cleanup () {
		doNothing ();
	}

	@Override
	public final
	void execute (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"execute");

			OwnedTransaction transaction =
				database.beginReadOnly (
					taskLogger,
					"ConsoleResponder.execute ()",
					this)

		) {

			this.transaction =
				transaction;

			setup (
				taskLogger);

			prepare (
				taskLogger);

			setHtmlHeaders (
				taskLogger);

			render (
				taskLogger);

		} finally {

			this.transaction =
				null;

			cleanup ();

		}

	}

}
