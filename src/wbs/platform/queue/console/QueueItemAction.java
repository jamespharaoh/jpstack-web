package wbs.platform.queue.console;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.queue.model.QueueItemRec;

import wbs.web.responder.Responder;

@PrototypeComponent ("queueItemAction")
public
class QueueItemAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	QueueItemConsoleHelper queueItemHelper;

	@SingletonDependency
	QueueManager queuePageFactoryManager;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// details

	@Override
	public
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return responder (
			"queueHomeResponder");

	}

	@Override
	protected
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnlyWithoutParameters (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

			Long queueItemId =
				Long.parseLong (
					requestContext.parameterRequired (
						"id"));

			QueueItemRec queueItem =
				queueItemHelper.findRequired (
					transaction,
					queueItemId);

			return queuePageFactoryManager.getItemResponder (
				transaction,
				requestContext,
				queueItem);

		}

	}

}