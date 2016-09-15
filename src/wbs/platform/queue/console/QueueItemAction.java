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

@PrototypeComponent ("queueItemAction")
public
class QueueItemAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	Database database;

	@SingletonDependency
	QueueItemConsoleHelper queueItemHelper;

	@SingletonDependency
	QueueManager queuePageFactoryManager;

	// details

	@Override
	public
	Responder backupResponder () {
		return responder ("queueHomeResponder");
	}

	@Override
	protected
	Responder goReal () {

		Long queueItemId =
			Long.parseLong (
				requestContext.parameterRequired (
					"id"));

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"QueueItemAction.goReal ()",
				this);

		QueueItemRec queueItem =
			queueItemHelper.findRequired (
				queueItemId);

		return queuePageFactoryManager.getItemResponder (
			requestContext,
			queueItem);

	}

}