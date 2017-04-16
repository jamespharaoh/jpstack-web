package wbs.web.responder;

import java.io.IOException;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;

import wbs.web.context.RequestContext;

public
class AbstractResponder
	implements Responder {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	RequestContext requestContext;

	// implementation

	protected
	void setup ()
		throws IOException {
	}

	protected
	void tearDown ()
		throws IOException {
	}

	protected
	void prepare () {
	}

	protected
	void goHeaders ()
		throws IOException {
	}

	protected
	void goContent ()
		throws IOException {
	}

	@Override
	public final
	void execute (
			@NonNull TaskLogger parentTaskLogger)
		throws IOException {

		try (

			Transaction transaction =
				database.beginReadOnly (
					"AbstractResponder.execute ()",
					this);

		) {

			setup ();

			try {

				prepare ();

				goHeaders ();

				goContent ();

			} finally {

				tearDown ();

			}

		}

	}

}
