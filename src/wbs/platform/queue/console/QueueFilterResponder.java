package wbs.platform.queue.console;

import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.joinWithSeparator;

import java.io.IOException;

import javax.inject.Inject;

import org.joda.time.Instant;

import wbs.console.misc.TimeFormatter;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.web.Responder;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;
import lombok.Cleanup;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;

@PrototypeComponent ("queueFilterResponder")
public
class QueueFilterResponder
	implements  Responder {

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	TimeFormatter timeFormatter;

	@Inject
	SliceObjectHelper sliceHelper;

	@Inject
	UserObjectHelper userHelper;

	@Inject
	Database database;

	@Override
	public
	void execute ()
		throws IOException {

		requestContext.setHeader (
			"Content-Type",
			"text/plain");

		requestContext.setHeader (
			"Cache-Control",
			"no-cache");

		requestContext.setHeader (
			"Expiry",
			timeFormatter.instantToHttpTimestampString (
				Instant.now ()));

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				this);

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		SliceRec currentSlice =
			myUser.getSlice ();

		String filter =
			ifNull (
				currentSlice.getFilter (),
				defaultFilter);

		requestContext.outputStream ().write (
			filter.getBytes ());

	}

	final static
	String defaultFilter =
		joinWithSeparator (
			"\n",
			"---",
			"- name: No Filter",
			"  options:",
			"    - name: All");

}
