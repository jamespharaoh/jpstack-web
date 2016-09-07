package wbs.platform.queue.console;

import static wbs.framework.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.framework.utils.etc.LogicUtils.referenceEqualWithClass;
import static wbs.framework.utils.etc.OptionalUtils.optionalIsPresent;

import javax.inject.Inject;
import javax.servlet.ServletException;

import lombok.Cleanup;
import lombok.NonNull;
import wbs.console.action.ConsoleAction;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueItemState;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("queueItemActionsAction")
public
class QueueItemActionsAction
	extends ConsoleAction {

	// dependencies

	@Inject
	Database database;

	@Inject
	QueueConsoleLogic queueConsoleLogic;

	@Inject
	QueueItemConsoleHelper queueItemHelper;

	@Inject
	UserConsoleLogic userConsoleLogic;

	// state

	UserRec user;

	QueueItemRec queueItem;

	boolean canSupervise;

	// details

	@Override
	protected
	Responder backupResponder () {

		return responder (
			"queueItemActionsResponder");

	}

	// implementation

	@Override
	protected
	Responder goReal ()
		throws ServletException {

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"QueueItemActionsAction.goReal ()",
				this);

		// load data

		user =
			userConsoleLogic.userRequired ();

		queueItem =
			queueItemHelper.findRequired (
				requestContext.stuffInteger (
					"queueItemId"));

		canSupervise =
			queueConsoleLogic.canSupervise (
				queueItem.getQueue ());

		if (! canSupervise) {

			requestContext.addError (
				"Permission denied");

			return null;

		}

		// hand off to appropriate method

		if (
			optionalIsPresent (
				requestContext.parameter (
					"unclaim"))
		) {

			return unclaimQueueItem (
				transaction);

		} else if (
			optionalIsPresent (
				requestContext.parameter (
					"reclaim"))
		) {

			return reclaimQueueItem (
				transaction);

		} else {

			throw new RuntimeException ();

		}

	}

	private
	Responder unclaimQueueItem (
			@NonNull Transaction transaction) {

		if (

			enumNotEqualSafe (
				queueItem.getState (),
				QueueItemState.claimed)

		) {

			requestContext.addError (
				"Queue item is not claimed");

			return null;

		}

		queueConsoleLogic.unclaimQueueItem (
			queueItem,
			user);

		transaction.commit ();

		requestContext.addNotice (
			"Queue item returned to queue");

		return null;

	}

	private
	Responder reclaimQueueItem (
			@NonNull Transaction transaction) {

		if (

			enumNotEqualSafe (
				queueItem.getState (),
				QueueItemState.claimed)

		) {

			requestContext.addWarning (
				"Queue item is not claimed");

			return null;

		}

		if (

			referenceEqualWithClass (
				UserRec.class,
				queueItem.getQueueItemClaim ().getUser (),
				user)

		) {

			requestContext.addWarning (
				"Queue item is already claimed by you");

			return null;

		}

		queueConsoleLogic.reclaimQueueItem (
			queueItem,
			queueItem.getQueueItemClaim ().getUser (),
			user);

		transaction.commit ();

		requestContext.addNotice (
			"Queue item reclaimed by you");

		return null;

	}

}
