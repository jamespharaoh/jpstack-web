package wbs.platform.queue.console;

import static wbs.utils.etc.LogicUtils.referenceNotEqualWithClass;

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
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserRec;

import wbs.web.responder.Responder;

@PrototypeComponent ("queueUnclaimAction")
public
class QueueUnclaimAction
	extends ConsoleAction {

	// dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	QueueConsoleLogic queueConsoleLogic;

	@SingletonDependency
	QueueItemConsoleHelper queueItemHelper;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// details

	@Override
	public
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return responder (
			"queueHomeResponder");

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

		long queueItemId =
			requestContext.parameterIntegerRequired (
				"queueItemId");

		try (

			Transaction transaction =
				database.beginReadWrite (
					taskLogger,
					"QueueUnclaimAction.goReal ()",
					this);

		) {

			QueueItemRec queueItem =
				queueItemHelper.findRequired (
					queueItemId);

			if (
				referenceNotEqualWithClass (
					UserRec.class,
					queueItem.getQueueItemClaim ().getUser (),
					userConsoleLogic.userRequired ())
			) {

				requestContext.addError (
					"Queue item is not claimed by you");

				return null;

			}

			queueConsoleLogic.unclaimQueueItem (
				queueItem,
				userConsoleLogic.userRequired ());

			transaction.commit ();

			requestContext.addNotice (
				"Queue item returned to queue");

			return null;

		}

	}

}