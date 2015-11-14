package wbs.framework.web;

import java.io.IOException;

import javax.inject.Inject;

import lombok.Cleanup;

import wbs.framework.database.Database;
import wbs.framework.database.Transaction;

public
class AbstractResponder
	implements Responder {

	// dependencies

	@Inject
	Database database;

	@Inject
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
	void execute ()
		throws IOException {

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				this);

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
