package wbs.platform.queue.console;

import lombok.Cleanup;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
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

	// singleton dependencies

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	QueueConsoleHelper queueHelper;

	@SingletonDependency
	QueueConsoleLogic queueConsoleLogic;

	@SingletonDependency
	Database database;

	@SingletonDependency
	QueueManager queuePageFactoryManager;

	@SingletonDependency
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

		Long queueId =
			Long.parseLong (
				requestContext.parameterRequired (
					"queue_id"));

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"QueueClaimAction.goReal ()",
				this);

		QueueRec queue =
			queueHelper.findRequired (
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