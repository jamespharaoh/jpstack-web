package wbs.platform.queue.logic;

import static wbs.framework.utils.etc.Misc.disallowNulls;
import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Date;

import javax.inject.Inject;

import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.object.ObjectHelper;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.Record;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.platform.queue.model.QueueItemClaimStatus;
import wbs.platform.queue.model.QueueItemObjectHelper;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueItemState;
import wbs.platform.queue.model.QueueObjectHelper;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.queue.model.QueueSubjectObjectHelper;
import wbs.platform.queue.model.QueueSubjectRec;
import wbs.platform.queue.model.QueueTypeObjectHelper;
import wbs.platform.queue.model.QueueTypeRec;
import wbs.platform.user.model.UserRec;

@Log4j
@SingletonComponent ("queueLogic")
public
class QueueLogicImpl
	implements QueueLogic {

	// dependencies

//	@Inject
//	ObjectDao objectDao;

	@Inject
	ObjectManager objectManager;

	@Inject
	ObjectTypeObjectHelper objectTypeHelper;

//	@Inject
//	PrivDao privDao;
//
//	@Inject
//	QueueDao queueDao;
//
//	@Inject
//	QueueSubjectObjectHelper queueSubjectDao;

	@Inject
	QueueObjectHelper queueHelper;

	@Inject
	QueueItemObjectHelper queueItemHelper;

	@Inject
	QueueSubjectObjectHelper queueSubjectHelper;

	@Inject
	QueueTypeObjectHelper queueTypeHelper;

	// implementation

	@Override
	public
	QueueRec findQueue (
			Record<?> parentObject,
			String code) {

		QueueRec queue =
			queueHelper.findByCode (
				parentObject,
				code);

		if (queue == null) {

			throw new RuntimeException (
				stringFormat (
					"Can't find queue %s for %s",
					code,
					objectManager.objectPath (parentObject)));

		}

		return queue;
	}

	@Override
	public
	QueueRec findOrCreateQueue (
			Record<?> parent,
			String queueTypeCode,
			String code) {

		ObjectHelper<?> parentHelper =
			objectManager.objectHelperForObject (
				parent);

		ObjectTypeRec parentType =
			objectTypeHelper.find (
				parentHelper.objectTypeId ());

		// lookup existing queue

		QueueRec queue =
			objectManager.findChildByCode (
				QueueRec.class,
				parent,
				code);

		if (queue == null) {

			QueueTypeRec queueType =
				queueTypeHelper.findByCode (
					parentType,
					queueTypeCode);

			if (queueType == null) {

				throw new RuntimeException (
					stringFormat (
						"No such queue type %s for object type %s",
						queueTypeCode,
						parentHelper.objectName ()));

			}

			String objectPath =
				objectManager.objectPath (
					parent,
					null,
					true,
					false);

			log.info (
				stringFormat (
					"Creating new queue %s of type %s on %s",
					code,
					queueTypeCode,
					objectPath));

			// create new queue

			queue =
				queueHelper.insert (
					new QueueRec ()
						.setCode (code)
						.setDescription (queueType.getDescription ())
						.setQueueType (queueType)
						.setParentObjectType (parentType)
						.setParentObjectId (parent.getId ()));

		}

		return queue;

	}

	@Override
	public
	QueueItemRec createQueueItem (
			QueueSubjectRec queueSubject,
			Record<?> refObject,
			String source,
			String details) {

		Date now =
			new Date ();

		QueueRec queue =
			queueSubject.getQueue ();

		QueueTypeRec queueType =
			queue.getQueueType ();

		// sanity check

		if (objectManager.getObjectTypeId (refObject)
				!= queueType.getRefObjectType ().getId ())
			throw new IllegalArgumentException ();

		// create the queue item

		boolean waiting =
			queueSubject.getActiveItems () > 0;

		QueueItemRec queueItem =
			queueItemHelper.insert (
				new QueueItemRec ()

					.setQueueSubject (queueSubject)
					.setIndex (queueSubject.getTotalItems ())

					.setQueue (queue)

					.setSource (source)
					.setDetails (details)
					.setRefObjectId (refObject.getId ())

					.setState (waiting
						? QueueItemState.waiting
						: QueueItemState.pending)

					.setCreatedTime (now)
					.setPendingTime (waiting
						? null
						: now));

		// update queue subject

		queueSubject.setTotalItems (
			queueSubject.getTotalItems () + 1);

		queueSubject.setActiveItems (
			queueSubject.getActiveItems () + 1);

		// and return

		return queueItem;

	}

	@Override
	public QueueItemRec createQueueItem (
			QueueRec queue,
			Record<?> subjectObject,
			Record<?> refObject,
			String source,
			String details) {

		QueueSubjectRec queueSubject =
			findOrCreateQueueSubject (
				queue,
				subjectObject);

		return createQueueItem (
			queueSubject,
			refObject,
			source,
			details);

	}

	@Override
	public
	QueueSubjectRec findOrCreateQueueSubject (
			QueueRec queue,
			Record<?> object) {

		QueueTypeRec queueType =
			queue.getQueueType ();

		// sanity check

		if (objectManager.getObjectTypeId (object)
				!= queueType.getSubjectObjectType ().getId ())
			throw new IllegalArgumentException ();

		// lookup existing

		QueueSubjectRec queueSubject =
			queueSubjectHelper.find (
				queue,
				object);

		if (queueSubject != null)
			return queueSubject;

		// create new

		queueSubject =
			queueSubjectHelper.insert (
				new QueueSubjectRec ()
					.setQueue (queue)
					.setObjectId (object.getId ()));

		return queueSubject;

	}

	@Override
	public
	void cancelQueueItem (
			QueueItemRec queueItem) {

		QueueSubjectRec queueSubject =
			queueItem.getQueueSubject ();

		Date now = new Date ();

		// sanity checks

		int currentItemIndex =
			+ queueSubject.getTotalItems ()
			- queueSubject.getActiveItems ();

		if (queueItem.getIndex () != currentItemIndex)
			throw new IllegalStateException ();

		if (! in (queueItem.getState (),
				QueueItemState.pending,
				QueueItemState.claimed)) {

			throw new RuntimeException (
				stringFormat (
					"Cannot cancel queue item in %s state",
					queueItem.getState ()));

		}

		// update queue item claim

		queueItem.getQueueItemClaim ()
			.setEndTime (now)
			.setStatus (QueueItemClaimStatus.cancelled);

		// update the queue item

		queueItem
			.setState (QueueItemState.cancelled)
			.setCancelledTime (now)
			.setQueueItemClaim (null);

		// update the queue subject

		queueSubject
			.setActiveItems (queueSubject.getActiveItems () - 1);

		// activate next queue item (if any)

		if (queueSubject.getActiveItems () > 0) {

			int nextItemIndex =
				+ queueSubject.getTotalItems ()
				- queueSubject.getActiveItems ();

			QueueItemRec nextQueueItem =
				queueItemHelper.findByIndex (
					queueSubject,
					nextItemIndex);

			if (nextQueueItem.getState () != QueueItemState.waiting)
				throw new IllegalStateException ();

			nextQueueItem
				.setState (QueueItemState.pending)
				.setPendingTime (now);

		}

	}

	@Override
	public
	void processQueueItem (
			QueueItemRec queueItem,
			UserRec user) {

		disallowNulls (queueItem, user);

		QueueSubjectRec queueSubject =
			queueItem.getQueueSubject ();

		Date now = new Date ();

		// sanity checks

		int currentItemIndex =
			+ queueSubject.getTotalItems ()
			- queueSubject.getActiveItems ();

		if (queueItem.getIndex () != currentItemIndex)
			throw new IllegalStateException ();

		if (queueItem.getState () != QueueItemState.claimed) {

			throw new RuntimeException (
				stringFormat (
					"Cannot process queue item %s in state %s",
					queueItem.getId (),
					queueItem.getState ()));

		}

		if (queueItem.getQueueItemClaim ().getUser () != user) {

			throw new RuntimeException (
				"Trying to process item belonging to another user");

		}

		// update queue item claim

		queueItem.getQueueItemClaim ()
			.setEndTime (now)
			.setStatus (QueueItemClaimStatus.processed);

		// update the queue item

		queueItem
			.setState (QueueItemState.processed)
			.setProcessedTime (now)
			.setProcessedUser (user)
			.setProcessedByPreferredUser (
				queueSubject.getPreferredUser () == null
					? null
					: queueSubject.getPreferredUser () == user)
			.setQueueItemClaim (null);

		// update the queue subject

		queueSubject
			.setActiveItems (queueSubject.getActiveItems () - 1)
			.setPreferredUser (user);

		// activate next queue item (if any)

		if (queueSubject.getActiveItems () > 0) {

			int nextItemIndex =
				+ queueSubject.getTotalItems ()
				- queueSubject.getActiveItems ();

			QueueItemRec nextQueueItem =
				queueItemHelper.findByIndex (
					queueSubject,
					nextItemIndex);

			if (nextQueueItem.getState () != QueueItemState.waiting)
				throw new IllegalStateException ();

			nextQueueItem
				.setState (QueueItemState.pending)
				.setPendingTime (now);

		}

	}

}
