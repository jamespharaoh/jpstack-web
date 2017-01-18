package wbs.platform.queue.console;

import static wbs.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.utils.etc.LogicUtils.referenceEqualWithClass;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import javax.servlet.ServletException;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;

import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueItemState;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserRec;

import wbs.web.responder.Responder;

@PrototypeComponent ("queueItemActionsAction")
public
class QueueItemActionsAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	QueueConsoleLogic queueConsoleLogic;

	@SingletonDependency
	QueueItemConsoleHelper queueItemHelper;

	@SingletonDependency
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
	Responder goReal (
			@NonNull TaskLogger taskLogger)
		throws ServletException {

		// begin transaction

		try (

			Transaction transaction =
				database.beginReadWrite (
					"QueueItemActionsAction.goReal ()",
					this);

		) {

			// load data

			user =
				userConsoleLogic.userRequired ();

			queueItem =
				queueItemHelper.findFromContextRequired ();

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
