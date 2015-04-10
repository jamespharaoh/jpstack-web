package wbs.platform.console.responder;

import java.io.IOException;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;

public abstract
class ConsoleResponder
	implements Responder {

	@Inject
	Database database;

	protected
	void setup ()
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
		prepare ();
		goHeaders ();
		goContent ();

	}

}
