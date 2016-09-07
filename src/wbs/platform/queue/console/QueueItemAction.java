package wbs.platform.queue.console;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.queue.model.QueueItemRec;

@PrototypeComponent ("queueItemAction")
public
class QueueItemAction
	extends ConsoleAction {

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	QueueItemConsoleHelper queueItemHelper;

	@Inject
	QueueManager queuePageFactoryManager;

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