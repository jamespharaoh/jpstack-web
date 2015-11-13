package wbs.platform.queue.console;

import static wbs.framework.utils.etc.Misc.instantToDate;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.platform.queue.model.QueueItemClaimObjectHelper;
import wbs.platform.queue.model.QueueItemClaimRec;
import wbs.platform.queue.model.QueueItemClaimStatus;
import wbs.platform.queue.model.QueueItemObjectHelper;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueItemState;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.queue.model.QueueSubjectRec;
import wbs.platform.user.model.UserRec;

@SingletonComponent ("queueConsoleLogic")
public
class QueueConsoleLogic {

	// dependencies

	@Inject
	Database database;

	@Inject
	ObjectManager objectManager;

	@Inject
	QueueItemClaimObjectHelper queueItemClaimHelper;

	@Inject
	QueueItemObjectHelper queueItemHelper;

	@Inject
	Provider<QueueSubjectSorter> queueSubjectSorter;

	// implementation

	public
	QueueItemRec claimQueueItem (
			QueueRec queue,
			UserRec user) {

		Transaction transaction =
			database.currentTransaction ();

		// find the next waiting item

		QueueSubjectSorter sorter =
			queueSubjectSorter.get ()
				.queue (queue)
				.user (user)
				.sort ();

		if (sorter.subjects ().isEmpty ())
			return null;

		QueueSubjectRec queueSubject =
			sorter.subjects ().get (0).subject ();

		int nextQueueItemId =
			+ queueSubject.getTotalItems ()
			- queueSubject.getActiveItems ();

		QueueItemRec queueItem =
			queueItemHelper.findByIndex (
				queueSubject,
				nextQueueItemId);

		// sanity checks

		if (queueItem.getState () != QueueItemState.pending)
			throw new IllegalStateException ();

		if (queueItem.getQueueItemClaim () != null)
			throw new IllegalStateException ();

		// create queue item claim

		QueueItemClaimRec queueItemClaim =
			queueItemClaimHelper.insert (
				queueItemClaimHelper.createInstance ()

			.setQueueItem (
				queueItem)

			.setUser (
				user)

			.setStartTime (
				instantToDate (
					transaction.now ()))

			.setStatus (
				QueueItemClaimStatus.claimed)

		);

		// update queue item

		queueItem

			.setState (
				QueueItemState.claimed)

			.setQueueItemClaim (
				queueItemClaim);

		// and return

		return queueItem;

	}

	public
	void unclaimQueueItem (
			QueueItemRec queueItem,
			UserRec user) {

		Transaction transaction =
			database.currentTransaction ();

		QueueSubjectRec queueSubject =
			queueItem.getQueueSubject ();

		// sanity checks

		int currentItemIndex =
			+ queueSubject.getTotalItems ()
			- queueSubject.getActiveItems ();

		if (queueItem.getIndex () != currentItemIndex)
			throw new IllegalStateException ();

		if (queueItem.getState () != QueueItemState.claimed) {

			throw new RuntimeException (
				stringFormat (
					"Cannot unclaim queue item in %s state",
					queueItem.getState ()));

		}

		if (queueItem.getQueueItemClaim ().getUser () != user) {

			throw new RuntimeException (
				"Trying to unclaim item belonging to another user");

		}

		// update queue item claim

		queueItem.getQueueItemClaim ()

			.setEndTime (
				instantToDate (
					transaction.now ()))

			.setStatus (
				QueueItemClaimStatus.unclaimed);

		// update the queue item

		queueItem

			.setState (
				QueueItemState.pending)

			.setQueueItemClaim (
				null);

	}

	public
	void reclaimQueueItem (
			QueueItemRec queueItem,
			UserRec oldUser,
			UserRec newUser) {

		Transaction transaction =
			database.currentTransaction ();

		QueueSubjectRec queueSubject =
			queueItem.getQueueSubject ();

		// sanity checks

		int currentItemIndex =
			+ queueSubject.getTotalItems ()
			- queueSubject.getActiveItems ();

		if (queueItem.getIndex () != currentItemIndex)
			throw new IllegalStateException ();

		if (queueItem.getState () != QueueItemState.claimed) {

			throw new IllegalStateException (
				stringFormat (
					"Cannot reclaim queue item in %s state",
					queueItem.getState ()));

		}

		if (queueItem.getQueueItemClaim ().getUser () != oldUser) {

			throw new IllegalStateException (
				"Item being reclaimed does not belong to expected user");

		}

		// update old queue item claim

		queueItem.getQueueItemClaim ()

			.setEndTime (
				instantToDate (
					transaction.now ()))

			.setStatus (
				QueueItemClaimStatus.forcedUnclaim);

		// create new queue item claim

		QueueItemClaimRec queueItemClaim =
			queueItemClaimHelper.insert (
				queueItemClaimHelper.createInstance ()

			.setQueueItem (
				queueItem)

			.setUser (
				newUser)

			.setStartTime (
				instantToDate (
					transaction.now ()))

			.setStatus (
				QueueItemClaimStatus.claimed)

		);

		// update queue item

		queueItem

			.setQueueItemClaim (
				queueItemClaim);

	}

}
