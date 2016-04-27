package wbs.platform.queue.console;

import javax.inject.Inject;

import lombok.Cleanup;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.user.console.UserConsoleLogic;

@PrototypeComponent ("queueClaimAction")
public
class QueueClaimAction
	extends ConsoleAction {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	QueueConsoleHelper queueHelper;

	@Inject
	QueueConsoleLogic queueConsoleLogic;

	@Inject
	Database database;

	@Inject
	QueueManager queuePageFactoryManager;

	@Inject
	UserConsoleLogic userConsoleLogic;

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

		int queueId =
			Integer.parseInt (
				requestContext.parameter ("queue_id"));

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		QueueRec queue =
			queueHelper.find (
				queueId);

		QueueItemRec queueItem =
			queueConsoleLogic.claimQueueItem (
				queue,
				userConsoleLogic.userRequired ());

		if (queueItem == null) {

			requestContext.addError (
				"No more items to claim in this queue");

			return null;

		}

		Responder responder =
			queuePageFactoryManager.getItemResponder (
				requestContext,
				queueItem);

		transaction.commit ();

		return responder;

	}

}