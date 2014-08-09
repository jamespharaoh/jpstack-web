package wbs.platform.queue.console;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("queueUnclaimAction")
public
class QueueUnclaimAction
	extends ConsoleAction {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	QueueItemConsoleHelper queueItemHelper;

	@Inject
	QueueConsoleLogic queueConsoleLogic;

	@Inject
	UserObjectHelper userHelper;

	// details

	@Override
	public
	Responder backupResponder () {
		return responder ("queueHomeResponder");
	}

	// implementation

	@Override
	protected
	Responder goReal () {

		int queueItemId =
			Integer.parseInt (
				requestContext.parameter ("queueItemId"));

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		QueueItemRec queueItem =
			queueItemHelper.find (queueItemId);

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		if (queueItem.getQueueItemClaim ().getUser () != myUser) {

			requestContext.addError (
				"Queue item is not claimed by you");

			return null;

		}

		queueConsoleLogic.unclaimQueueItem (
			queueItem,
			myUser);

		transaction.commit ();

		requestContext.addNotice (
			"Queue item returned to queue");

		return null;

	}

}