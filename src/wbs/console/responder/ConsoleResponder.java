package wbs.console.responder;

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

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				this);

		setup ();
		prepare ();
		setHtmlHeaders ();
		render ();

	}

}
