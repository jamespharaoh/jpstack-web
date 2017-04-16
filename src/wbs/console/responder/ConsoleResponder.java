package wbs.console.responder;

import static wbs.utils.etc.Misc.doNothing;

import java.io.IOException;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
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
	Transaction transaction;

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
	void setHtmlHeaders ()
		throws IOException {
	}

	protected
	void render (
			@NonNull TaskLogger parentTaskLogger) {

	}

	protected
	void cleanup () {
		doNothing ();
	}

	@Override
	public final
	void execute (
			@NonNull TaskLogger parentTaskLogger)
		throws IOException {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"execute");

		try (

			Transaction transaction =
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

			setHtmlHeaders ();

			render (
				taskLogger);

		} finally {

			this.transaction =
				null;

			cleanup ();

		}

	}

}
