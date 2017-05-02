package wbs.web.responder;

import static wbs.utils.etc.Misc.doNothing;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
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
			@NonNull Transaction parentTransaction) {

		doNothing ();

	}

	protected
	void tearDown (
			@NonNull Transaction parentTransaction) {

		doNothing ();

	}

	protected
	void prepare (
			@NonNull Transaction parentTransaction) {

		doNothing ();

	}

	protected
	void goHeaders (
			@NonNull Transaction parentTransaction) {

		doNothing ();

	}

	protected
	void goContent (
			@NonNull Transaction parentTransaction) {

		doNothing ();

	}

	@Override
	public final
	void execute (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"execute");

		) {

			setup (
				transaction);

			try {

				prepare (
					transaction);

				goHeaders (
					transaction);

				goContent (
					transaction);

			} finally {

				tearDown (
					transaction);

			}

		}

	}

}
