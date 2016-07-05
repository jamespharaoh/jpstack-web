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

		int queueItemId =
			Integer.parseInt (
				requestContext.parameterOrNull ("id"));

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				this);

		QueueItemRec queueItem =
			queueItemHelper.findOrNull (queueItemId);

		if (queueItem == null) {

			requestContext.addError (
				"Queue item not found");

			return null;

		}

		return queuePageFactoryManager.getItemResponder (
			requestContext,
			queueItem);

	}

}