package wbs.platform.queue.console;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.web.responder.Responder;

@PrototypeComponent ("queueClaimAction")
public
class QueueClaimAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	QueueConsoleLogic queueConsoleLogic;

	@SingletonDependency
	QueueConsoleHelper queueHelper;

	@SingletonDependency
	QueueManager queuePageFactoryManager;

	@SingletonDependency
	ConsoleRequestContext requestContext;

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
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"goReal");

		Long queueId =
			Long.parseLong (
				requestContext.parameterRequired (
					"queue_id"));

		try (

			Transaction transaction =
				database.beginReadWrite (
					"QueueClaimAction.goReal ()",
					this);

		) {

			QueueRec queue =
				queueHelper.findRequired (
					queueId);

			QueueItemRec queueItem =
				queueConsoleLogic.claimQueueItem (
					taskLogger,
					queue,
					userConsoleLogic.userRequired ());

			if (queueItem == null) {

				requestContext.addError (
					"No more items to claim in this queue");

				return null;

			}

			Responder responder =
				queuePageFactoryManager.getItemResponder (
					taskLogger,
					requestContext,
					queueItem);

			transaction.commit ();

			return responder;

		}

	}

}