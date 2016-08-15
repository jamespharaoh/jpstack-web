package wbs.platform.queue.console;

import static wbs.framework.utils.etc.CodeUtils.simplifyToCodeRequired;
import static wbs.framework.utils.etc.Misc.isNotInstanceOf;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.StringUtils.camelToUnderscore;
import static wbs.framework.utils.etc.StringUtils.joinWithFullStop;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import org.joda.time.Instant;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.meta.ModelMetaLoader;
import wbs.framework.entity.meta.ModelMetaSpec;
import wbs.framework.object.ObjectManager;
import wbs.platform.queue.logic.DummyQueueCache;
import wbs.platform.queue.metamodel.QueueTypeSpec;
import wbs.platform.queue.metamodel.QueueTypesSpec;
import wbs.platform.queue.model.QueueItemClaimObjectHelper;
import wbs.platform.queue.model.QueueItemClaimRec;
import wbs.platform.queue.model.QueueItemClaimStatus;
import wbs.platform.queue.model.QueueItemObjectHelper;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueItemState;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.queue.model.QueueSubjectRec;
import wbs.platform.queue.model.QueueTypeRec;
import wbs.platform.scaffold.logic.SliceLogic;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserRec;

@SingletonComponent ("queueConsoleLogic")
public
class QueueConsoleLogic {

	// dependencies

	@Inject
	Database database;

	@Inject
	DummyQueueCache dummyQueueCache;

	@Inject
	ObjectManager objectManager;

	@Inject
	ModelMetaLoader modelMetaLoader;

	@Inject
	QueueItemClaimObjectHelper queueItemClaimHelper;

	@Inject
	QueueItemObjectHelper queueItemHelper;

	@Inject
	SliceLogic sliceLogic;

	@Inject
	UserConsoleLogic userConsoleLogic;

	// prototype dependencies

	@Inject
	Provider<QueueSubjectSorter> queueSubjectSorterProvider;

	// state

	Map<String,QueueTypeSpec> queueTypeSpecs;

	// lifecycle

	@PostConstruct
	public
	void setup () {

		// collect queue type definitions

		ImmutableMap.Builder<String,QueueTypeSpec> queueTypeSpecsBuilder =
			ImmutableMap.<String,QueueTypeSpec>builder ();

		for (
			ModelMetaSpec modelMeta
				: modelMetaLoader.modelMetas ().values ()
		) {

			for (
				Object childObject
					: modelMeta.children ()
			) {

				if (
					isNotInstanceOf (
						QueueTypesSpec.class,
						childObject)
				) {
					continue;
				}

				QueueTypesSpec queueTypes =
					(QueueTypesSpec)
					childObject;

				for (
					QueueTypeSpec queueType
						: queueTypes.queueTypes ()
				) {

					String queueTypeName =
						joinWithFullStop (
							camelToUnderscore (
								modelMeta.name ()),
							simplifyToCodeRequired (
								queueType.name ()));

					queueTypeSpecsBuilder.put (
						queueTypeName,
						queueType);

				}

			}

		}

		queueTypeSpecs =
			queueTypeSpecsBuilder.build ();

	}

	// implementation

	public
	QueueTypeSpec queueTypeSpec (
			@NonNull QueueTypeRec queueType) {

		String queueTypeName =
			joinWithFullStop (
				queueType.getParentType ().getCode (),
				queueType.getCode ());

		QueueTypeSpec queueTypeSpec =
			queueTypeSpecs.get (
				queueTypeName);

		if (
			isNull (
				queueTypeSpec)
		) {

			throw new RuntimeException (
				stringFormat (
					"No such queue type spec: %s",
					queueTypeName));

		}

		return queueTypeSpec;

	}

	public
	QueueItemRec claimQueueItem (
			@NonNull QueueRec queue,
			@NonNull UserRec user) {

		Transaction transaction =
			database.currentTransaction ();

		// find the next waiting item

		SortedQueueSubjects subjects =
			queueSubjectSorterProvider.get ()

			.queueCache (
				dummyQueueCache)

			.queue (
				queue)

			.loggedInUser (
				userConsoleLogic.userRequired ())

			.effectiveUser (
				user)

			.sort ();

		if (subjects.availableSubjects ().isEmpty ())
			return null;

		QueueSubjectRec queueSubject =
			subjects.availableSubjects ().get (0).subject ();

		long nextQueueItemId =
			+ queueSubject.getTotalItems ()
			- queueSubject.getActiveItems ();

		QueueItemRec queueItem =
			queueItemHelper.findByIndexOrNull (
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
				transaction.now ())

			.setStatus (
				QueueItemClaimStatus.claimed)

		);

		// update queue item

		queueItem

			.setState (
				QueueItemState.claimed)

			.setQueueItemClaim (
				queueItemClaim);

		// update slice

		sliceLogic.updateSliceInactivityTimestamp (
			user.getSlice (),
			Optional.<Instant>absent ());

		// and return

		return queueItem;

	}

	public
	void unclaimQueueItem (
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
				transaction.now ())

			.setStatus (
				QueueItemClaimStatus.unclaimed);

		// update the queue item

		queueItem

			.setState (
				QueueItemState.pending)

			.setQueueItemClaim (
				null);

		// update slice

		sliceLogic.updateSliceInactivityTimestamp (
			user.getSlice (),
			Optional.<Instant>absent ());

	}

	public
	void reclaimQueueItem (
			@NonNull QueueItemRec queueItem,
			@NonNull UserRec oldUser,
			@NonNull UserRec newUser) {

		Transaction transaction =
			database.currentTransaction ();

		QueueSubjectRec queueSubject =
			queueItem.getQueueSubject ();

		// sanity checks

		long currentItemIndex =
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
				transaction.now ())

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
				transaction.now ())

			.setStatus (
				QueueItemClaimStatus.claimed)

		);

		// update queue item

		queueItem

			.setQueueItemClaim (
				queueItemClaim);

		// update slice

		sliceLogic.updateSliceInactivityTimestamp (
			newUser.getSlice (),
			Optional.<Instant>absent ());

	}

}
