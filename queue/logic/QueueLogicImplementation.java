package wbs.platform.queue.logic;

import static wbs.utils.etc.EnumUtils.enumNameSpaces;
import static wbs.utils.etc.EnumUtils.enumNotInSafe;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NumberUtils.integerNotEqualSafe;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.time.TimeUtils.laterThan;

import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.object.ObjectManager;

import wbs.platform.object.core.model.ObjectTypeObjectHelper;
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
import wbs.platform.scaffold.logic.SliceLogic;
import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.user.model.UserRec;

@Log4j
@SingletonComponent ("queueLogic")
public
class QueueLogicImplementation
	implements QueueLogic {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	ObjectTypeObjectHelper objectTypeHelper;

	@SingletonDependency
	QueueObjectHelper queueHelper;

	@SingletonDependency
	QueueItemObjectHelper queueItemHelper;

	@SingletonDependency
	QueueSubjectObjectHelper queueSubjectHelper;

	@SingletonDependency
	QueueTypeObjectHelper queueTypeHelper;

	@SingletonDependency
	SliceLogic sliceLogic;

	// implementation

	@Override
	public
	QueueRec findQueue (
			@NonNull Record<?> parentObject,
			@NonNull String code) {

		return queueHelper.findByCodeRequired (
			parentObject,
			code);

	}

	@Override
	public
	QueueItemRec createQueueItem (
			@NonNull QueueSubjectRec queueSubject,
			@NonNull Record<?> refObject,
			@NonNull String source,
			@NonNull String details) {

		Transaction transaction =
			database.currentTransaction ();

		QueueRec queue =
			queueSubject.getQueue ();

		QueueTypeRec queueType =
			queue.getQueueType ();

		// sanity check

		if (
			integerNotEqualSafe (
				objectManager.getObjectTypeId (
					refObject),
				queueType.getRefType ().getId ())
		) {

			throw new IllegalArgumentException ();

		}

		// create the queue item

		boolean waiting =
			queueSubject.getActiveItems () > 0;

		QueueItemRec queueItem =
			queueItemHelper.insert (
				queueItemHelper.createInstance ()

			.setQueueSubject (
				queueSubject)

			.setIndex (
				queueSubject.getTotalItems ())

			.setQueue (
				queue)

			.setSource (
				source)

			.setDetails (
				details)

			.setRefObjectId (
				refObject.getId ())

			.setState (
				waiting
					? QueueItemState.waiting
					: QueueItemState.pending)

			.setCreatedTime (
				transaction.now ())

			.setPendingTime (
				waiting
					? null
					: transaction.now ())

			.setPriority (
				ifNull (
					queue.getDefaultPriority (),
					0l))

		);

		// update queue subject

		queueSubject

			.setTotalItems (
				queueSubject.getTotalItems () + 1)

			.setActiveItems (
				queueSubject.getActiveItems () + 1);

		// update slice

		Optional<SliceRec> optionalSlice =
			objectManager.getAncestor (
				SliceRec.class,
				queue);

		if (
			optionalIsNotPresent (
				optionalSlice)
		) {

			log.warn (
				stringFormat (
					"Unable to determine slice for queue %s",
					integerToDecimalString (
						queue.getId ())));

		}

		sliceLogic.updateSliceInactivityTimestamp (
			optionalSlice.get (),
			Optional.of (
				transaction.now ()));

		// and return

		return queueItem;

	}

	@Override
	public
	QueueItemRec createQueueItem (
			@NonNull QueueRec queue,
			@NonNull Record<?> subjectObject,
			@NonNull Record<?> refObject,
			@NonNull String source,
			@NonNull String details) {

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
			@NonNull QueueRec queue,
			@NonNull Record<?> object) {

		QueueTypeRec queueType =
			queue.getQueueType ();

		// sanity check

		if (
			integerNotEqualSafe (
				objectManager.getObjectTypeId (
					object),
				queueType.getSubjectType ().getId ())
		) {

			throw new IllegalArgumentException (
				stringFormat (
					"Queue %s expected subject type %s, got %s",
					objectManager.objectPath (
						queue),
					queueType.getSubjectType ().getCode (),
					objectManager.getObjectTypeCode (
						object)));

		}

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
				queueSubjectHelper.createInstance ()

			.setQueue (
				queue)

			.setObjectId (
				object.getId ())

		);

		return queueSubject;

	}

	@Override
	public
	void cancelQueueItem (
			@NonNull QueueItemRec queueItem) {

		Transaction transaction =
			database.currentTransaction ();

		QueueSubjectRec queueSubject =
			queueItem.getQueueSubject ();

		// sanity checks

		long currentItemIndex =
			+ queueSubject.getTotalItems ()
			- queueSubject.getActiveItems ();

		if (
			integerNotEqualSafe (
				queueItem.getIndex (),
				currentItemIndex)
		) {
			throw new IllegalStateException ();
		}

		if (
			enumNotInSafe (
				queueItem.getState (),
				QueueItemState.pending,
				QueueItemState.claimed)) {

			throw new RuntimeException (
				stringFormat (
					"Cannot cancel queue item in state: %s",
					enumNameSpaces (
						queueItem.getState ())));

		}

		// update queue item claim

		queueItem.getQueueItemClaim ()

			.setEndTime (
				transaction.now ())

			.setStatus (
				QueueItemClaimStatus.cancelled);

		// update the queue item

		queueItem

			.setState (
				QueueItemState.cancelled)

			.setCancelledTime (
				transaction.now ())

			.setQueueItemClaim (
				null);

		// update the queue subject

		queueSubject

			.setActiveItems (
				queueSubject.getActiveItems () - 1);

		// activate next queue item (if any)

		if (queueSubject.getActiveItems () > 0) {

			long nextItemIndex =
				+ queueSubject.getTotalItems ()
				- queueSubject.getActiveItems ();

			QueueItemRec nextQueueItem =
				queueItemHelper.findByIndexRequired (
					queueSubject,
					nextItemIndex);

			if (nextQueueItem.getState () != QueueItemState.waiting)
				throw new IllegalStateException ();

			nextQueueItem

				.setState (
					QueueItemState.pending)

				.setPendingTime (
					transaction.now ());

		}

	}

	@Override
	public
	void processQueueItem (
			@NonNull QueueItemRec queueItem,
			@NonNull UserRec user) {

		Transaction transaction =
			database.currentTransaction ();

		QueueSubjectRec queueSubject =
			queueItem.getQueueSubject ();

		// sanity checks

		long currentItemIndex =
			+ queueSubject.getTotalItems ()
			- queueSubject.getActiveItems ();

		if (queueItem.getIndex () != currentItemIndex) {

			throw new IllegalStateException (
				stringFormat (
					"Cannot process queue item %s ",
					integerToDecimalString (
						queueItem.getId ()),
					"with index %s ",
					integerToDecimalString (
						queueItem.getIndex ()),
					"for queue subject %s ",
					integerToDecimalString (
						queueSubject.getId ()),
					"whose total is %s ",
					integerToDecimalString (
						queueSubject.getTotalItems ()),
					"and active is %s, ",
					integerToDecimalString (
						queueSubject.getActiveItems ()),
					"implying a current item index of %s",
					integerToDecimalString (
						currentItemIndex)));

		}

		if (queueItem.getState () != QueueItemState.claimed) {

			throw new RuntimeException (
				stringFormat (
					"Cannot process queue item %s in state: %s",
					integerToDecimalString (
						queueItem.getId ()),
					enumNameSpaces (
						queueItem.getState ())));

		}

		if (queueItem.getQueueItemClaim ().getUser () != user) {

			throw new RuntimeException (
				"Trying to process item belonging to another user");

		}

		// update queue item claim

		queueItem.getQueueItemClaim ()

			.setEndTime (
				transaction.now ())

			.setStatus (
				QueueItemClaimStatus.processed);

		// update the queue item

		queueItem

			.setState (
				QueueItemState.processed)

			.setProcessedTime (
				transaction.now ())

			.setProcessedUser (
				user)

			.setProcessedByPreferredUser (
				queueSubject.getPreferredUser () == null
					? null
					: queueSubject.getPreferredUser () == user)

			.setQueueItemClaim (
				null);

		// update the queue subject

		queueSubject

			.setActiveItems (
				queueSubject.getActiveItems () - 1)

			.setPreferredUser (
				user);

		// update slice

		sliceLogic.updateSliceInactivityTimestamp (
			user.getSlice (),
			Optional.<Instant>absent ());

		// activate next queue item (if any)

		if (queueSubject.getActiveItems () > 0) {

			long nextItemIndex =
				+ queueSubject.getTotalItems ()
				- queueSubject.getActiveItems ();

			QueueItemRec nextQueueItem =
				queueItemHelper.findByIndexRequired (
					queueSubject,
					nextItemIndex);

			if (nextQueueItem.getState () != QueueItemState.waiting)
				throw new IllegalStateException ();

			nextQueueItem

				.setState (
					QueueItemState.pending)

				.setPendingTime (
					transaction.now ());

		}

	}

	@Override
	public
	List<QueueItemRec> getActiveQueueItems (
			@NonNull QueueSubjectRec queueSubject) {

		return queueSubject.getQueueItems ().subList (
			toJavaIntegerRequired (
				+ queueSubject.getTotalItems ()
				- queueSubject.getActiveItems ()),
			toJavaIntegerRequired (
				queueSubject.getTotalItems ()));

	}

	@Override
	public
	boolean sliceHasQueueActivity (
			@NonNull SliceRec slice) {

		Transaction transaction =
			database.currentTransaction ();

		// active if no config or stats

		if (

			isNull (
				slice.getQueueOverflowInactivityTime ())

			|| isNull (
				slice.getCurrentQueueInactivityTime ())

		) {
			return true;
		}

		// check for recent activity

		return laterThan (
			slice.getCurrentQueueInactivityTime ().plus (
				Duration.standardSeconds (
					slice.getQueueOverflowInactivityTime ())),
			transaction.now ());

	}

	@Override
	public
	QueueRec findQueueByCodeRequired (
			@NonNull Record <?> queueParent,
			@NonNull String queueCode) {

		return queueHelper.findByCodeRequired (
			queueParent,
			queueCode);

	}

}
