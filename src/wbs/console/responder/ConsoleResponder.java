package wbs.console.responder;

import java.io.IOException;

import javax.inject.Inject;

import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;

public abstract
class ConsoleResponder
	implements Responder {

	// dependencies

	@Inject
	Database database;

	// state

	protected
	Transaction transaction;

	// implementation

	protected
	void setup ()
		throws IOException {
	}

	protected
	void prepare () {
	}

	protected
	void setHtmlHeaders ()
		throws IOException {
	}

	protected
	void render ()
		throws IOException {
	}

	@Override
	public final
	void execute ()
		throws IOException {

		try (
			Transaction transaction =
				database.beginReadOnly (
					this)
		) {

			this.transaction =
				transaction;

			setup ();
			prepare ();
			setHtmlHeaders ();
			render ();

		} finally {

			this.transaction =
				null;

		}

	}

}
