package wbs.platform.queue.console;

import static wbs.framework.utils.etc.Misc.notEqual;

import javax.inject.Inject;

import lombok.Cleanup;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.user.console.UserConsoleLogic;

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

		long queueItemId =
			requestContext.parameterInteger (
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
			notEqual (
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