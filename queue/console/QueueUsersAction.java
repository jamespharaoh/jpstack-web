package wbs.platform.queue.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import java.util.List;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;

import wbs.platform.queue.model.QueueItemClaimObjectHelper;
import wbs.platform.queue.model.QueueItemClaimRec;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;

import wbs.web.responder.Responder;

@PrototypeComponent ("queueUsersAction")
public
class QueueUsersAction
	extends ConsoleAction {

	// dependencies

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	Database database;

	@SingletonDependency
	QueueItemClaimObjectHelper queueItemClaimHelper;

	@SingletonDependency
	QueueConsoleLogic queueConsoleLogic;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserObjectHelper userHelper;

	// details

	@Override
	public
	Responder backupResponder () {
		return responder ("queueUsersResponder");
	}

	// implementation

	@Override
	public
	Responder goReal (
			@NonNull TaskLogger taskLogger) {

		// get params

		long userId =
			requestContext.parameterIntegerRequired (
				"userId");

		boolean reclaim;

		if (
			optionalIsPresent (
				requestContext.parameter (
					"reclaim"))
		) {

			reclaim = true;

		} else if (
			optionalIsPresent (
				requestContext.parameter (
					"unclaim"))
		) {

			reclaim = false;

		} else {

			throw new RuntimeException ();

		}

		try (

			Transaction transaction =
				database.beginReadWrite (
					"QueueUsersAction.goReal ()",
					this);

		) {

			// load stuff

			UserRec theUser =
				userHelper.findRequired (
					userId);

			// load items

			List<QueueItemClaimRec> queueItemClaims =
				queueItemClaimHelper.findClaimed (
					theUser);

			int numQueueItems =
				queueItemClaims.size ();

			// process items

			for (QueueItemClaimRec queueItemClaim
					: queueItemClaims) {

				QueueItemRec queueItem =
					queueItemClaim.getQueueItem ();

				if (reclaim) {

					queueConsoleLogic.reclaimQueueItem (
						taskLogger,
						queueItem,
						theUser,
						userConsoleLogic.userRequired ());

				} else {

					queueConsoleLogic.unclaimQueueItem (
						queueItem,
						theUser);

				}

			}

			transaction.commit ();

			// add notice

			if (reclaim) {

				requestContext.addNoticeFormat (
					"Reclaimed %s queue items",
					integerToDecimalString (
						numQueueItems));

			} else {

				requestContext.addNoticeFormat (
					"Unclaimed %s queue items",
					integerToDecimalString (
						numQueueItems));

			}

			return null;

		}

	}

}
