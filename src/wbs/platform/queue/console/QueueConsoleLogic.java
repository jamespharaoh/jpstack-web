package wbs.platform.queue.console;

import static wbs.utils.etc.EnumUtils.enumName;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.etc.TypeUtils.isNotInstanceOf;
import static wbs.utils.string.CodeUtils.simplifyToCodeRequired;
import static wbs.utils.string.StringUtils.camelToUnderscore;
import static wbs.utils.string.StringUtils.joinWithFullStop;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Map;

import javax.inject.Provider;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.BorrowedTransaction;
import wbs.framework.database.Database;
import wbs.framework.entity.meta.model.ModelMetaLoader;
import wbs.framework.entity.meta.model.ModelMetaSpec;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
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

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	DummyQueueCache dummyQueueCache;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ModelMetaLoader modelMetaLoader;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	QueueItemClaimObjectHelper queueItemClaimHelper;

	@SingletonDependency
	QueueItemObjectHelper queueItemHelper;

	@SingletonDependency
	SliceLogic sliceLogic;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserPrivChecker privChecker;

	// prototype dependencies

	@PrototypeDependency
	Provider <QueueSubjectSorter> queueSubjectSorterProvider;

	// state

	Map <String, QueueTypeSpec> queueTypeSpecs;

	// lifecycle

	@NormalLifecycleSetup
	public
	void setup () {

		// collect queue type definitions

		ImmutableMap.Builder <String, QueueTypeSpec> queueTypeSpecsBuilder =
			ImmutableMap.builder ();

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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull QueueRec queue,
			@NonNull UserRec user) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"claimQueueItem");

		) {

			BorrowedTransaction transaction =
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

				.sort (
					taskLogger);

			if (subjects.availableSubjects ().isEmpty ())
				return null;

			QueueSubjectRec queueSubject =
				subjects.availableSubjects ().get (0).subject ();

			long nextQueueItemId =
				+ queueSubject.getTotalItems ()
				- queueSubject.getActiveItems ();

			QueueItemRec queueItem =
				queueItemHelper.findByIndexRequired (
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
					taskLogger,
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
				taskLogger,
				user.getSlice (),
				optionalAbsent ());

			// and return

			return queueItem;

		}

	}

	public
	void unclaimQueueItem (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull QueueItemRec queueItem,
			@NonNull UserRec user) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"unclaimQueueItem");

		) {

			BorrowedTransaction transaction =
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
						enumName (
							queueItem.getState ())));

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
				taskLogger,
				user.getSlice (),
				optionalAbsent ());

		}

	}

	public
	void reclaimQueueItem (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull QueueItemRec queueItem,
			@NonNull UserRec oldUser,
			@NonNull UserRec newUser) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"reclaimQueueItem");

		) {

			BorrowedTransaction transaction =
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
						enumName (
							queueItem.getState ())));

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
					taskLogger,
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
				taskLogger,
				newUser.getSlice (),
				optionalAbsent ());

		}

	}

	public
	boolean canSupervise (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull QueueRec queue) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"canSupervise");

		) {

			QueueTypeSpec queueTypeSpec =
				queueTypeSpec (
					queue.getQueueType ());

			String[] supervisorParts =
				queueTypeSpec.supervisorPriv ().split (":");

			Record <?> supervisorDelegate =
				genericCastUnchecked (
					objectManager.dereferenceRequired (
						queue,
						supervisorParts [0]));

			return privChecker.canRecursive (
				taskLogger,
				supervisorDelegate,
				supervisorParts [1]);

		}

	}

}
