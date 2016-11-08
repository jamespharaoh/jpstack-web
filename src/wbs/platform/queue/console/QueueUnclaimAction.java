package wbs.platform.queue.console;

import static wbs.utils.etc.LogicUtils.referenceNotEqualWithClass;

import lombok.Cleanup;
import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;
import wbs.framework.web.Responder;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("queueUnclaimAction")
public
class QueueUnclaimAction
	extends ConsoleAction {

	// dependencies

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	Database database;

	@SingletonDependency
	QueueItemConsoleHelper queueItemHelper;

	@SingletonDependency
	QueueConsoleLogic queueConsoleLogic;

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
			@NonNull TaskLogger taskLogger) {

		long queueItemId =
			requestContext.parameterIntegerRequired (
				"queueItemId");

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"QueueUnclaimAction.goReal ()",
				this);

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