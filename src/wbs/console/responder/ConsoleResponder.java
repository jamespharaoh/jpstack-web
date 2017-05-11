package wbs.console.responder;

import static wbs.utils.etc.Misc.doNothing;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
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
	OwnedTransaction transaction;

	// implementation

	protected
	void setup (
			@NonNull Transaction parentTransaction) {

		doNothing ();

	}

	protected
	void prepare (
			@NonNull Transaction parentTransaction) {

		doNothing ();

	}

	protected
	void setHtmlHeaders (
			@NonNull Transaction parentTransaction) {

		doNothing ();

	}

	protected
	void render (
			@NonNull Transaction parentTransaction) {

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

			OwnedTransaction transaction =
				database.beginReadOnlyWithoutParameters (
					logContext,
					parentTaskLogger,
					"execute");

		) {

			this.transaction =
				transaction;

			setup (
				transaction);

			prepare (
				transaction);

			setHtmlHeaders (
				transaction);

			render (
				transaction);

		} finally {

			this.transaction =
				null;

			cleanup ();

		}

	}

}
