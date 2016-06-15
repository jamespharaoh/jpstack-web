package wbs.platform.queue.console;

import static wbs.framework.utils.etc.Misc.earlierThan;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.isNotEmpty;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.laterThan;
import static wbs.framework.utils.etc.Misc.lessThan;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.notIn;
import static wbs.framework.utils.etc.Misc.optionalRequired;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Data;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.console.priv.UserPrivChecker;
import wbs.console.priv.UserPrivCheckerBuilder;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.Record;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.queue.model.QueueItemObjectHelper;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueItemState;
import wbs.platform.queue.model.QueueObjectHelper;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.queue.model.QueueSubjectObjectHelper;
import wbs.platform.queue.model.QueueSubjectRec;
import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.user.model.UserRec;

@Accessors (fluent = true)
@PrototypeComponent ("queueSubjectSorter")
public
class QueueSubjectSorter {

	// dependencies

	@Inject
	Database database;

	@Inject
	ObjectManager objectManager;

	@Inject
	UserPrivChecker loggedInUserPrivChecker;

	@Inject
	QueueObjectHelper queueHelper;

	@Inject
	QueueItemObjectHelper queueItemHelper;

	@Inject
	QueueLogic queueLogic;

	@Inject
	QueueManager queueManager;

	@Inject
	QueueSubjectObjectHelper queueSubjectHelper;

	// prototype dependencies

	@Inject
	Provider<UserPrivCheckerBuilder> userPrivCheckerBuilderProvider;

	// inputs

	@Setter
	QueueRec queue;

	@Setter
	UserRec effectiveUser;

	// state

	UserPrivChecker effectiveUserPrivChecker;

	Transaction transaction;

	Set<SubjectInfo> subjectInfos =
		new HashSet<SubjectInfo> ();

	Map<QueueRec,QueueInfo> queueInfos =
		new HashMap<QueueRec,QueueInfo> ();

	SortedQueueSubjects result =
		new SortedQueueSubjects ();

	// implementation

	public
	SortedQueueSubjects sort () {

		if (
			isNotNull (
				effectiveUser)
		) {

			effectiveUserPrivChecker =
				userPrivCheckerBuilderProvider.get ()

				.userId (
					(long) (int)
					effectiveUser.getId ())

				.build ();

		}

		transaction =
			database.currentTransaction ();

		List<QueueSubjectRec> queueSubjects =
			queue != null
				? queueSubjectHelper.findActive (queue)
				: queueSubjectHelper.findActive ();

		queueSubjects.forEach (
			this::processSubject);

		// convert subjects to list, filter and sort

		result.availableSubjects =
			subjectInfos.stream ()

			.filter (
				subjectInfo ->
					effectiveUser == null || subjectInfo.available)

			.sorted (
				effectiveUser != null
					? SubjectInfo.effectiveTimeComparator
					: SubjectInfo.createdTimeComparator)

			.collect (
				Collectors.toList ());

		// convert queues to list, filter and sort

		result.allQueues =
			queueInfos.values ().stream ()

			.sorted (
				effectiveUser != null
					? QueueInfo.oldestAvailableComparator
					: QueueInfo.oldestComparator)

			.filter (queueInfo ->
				isNotEmpty (
					queueInfo.subjectInfos))

			.collect (
				Collectors.toList ());

		// filter available queues

		result.availableQueues =
			result.allQueues.stream ()

			.filter (queueInfo ->
				effectiveUser == null || queueInfo.availableItems > 0)

			.collect (
				Collectors.toList ());

		// sort subjects in each queue

		result.allQueues.forEach (queueInfo ->
			Collections.sort (
				queueInfo.subjectInfos,
				effectiveUser != null
					? SubjectInfo.effectiveTimeComparator
					: SubjectInfo.createdTimeComparator));

		// and return

		return result;

	}

	public
	void processSubject (
			@NonNull QueueSubjectRec subject) {

		// get queue info

		QueueInfo queueInfo =
			queueInfos.computeIfAbsent (
				subject.getQueue (),
				this::createQueueInfo);

		// check we can see this queue

		if (
			! queueInfo.canReplyImplicit
			&& ! queueInfo.canReplyOverflowImplicit
		) {
			return;
		}

		// get subject info

		SubjectInfo subjectInfo =
			createSubjectInfo (
				queueInfo,
				subject);

		subjectInfos.add (
			subjectInfo);

		queueInfo.subjectInfos.add (
			subjectInfo);

		// count stuff

		countTotal (
			queueInfo,
			subjectInfo);

		countWaiting (
			queueInfo,
			subjectInfo);

		// claimed items are not available

		if (
			equal (
				subjectInfo.state,
				QueueItemState.claimed)
		) {

			countClaimed (
				queueInfo,
				subjectInfo);

			return;

		}

		// update queue

		updateQueueInfo (
			queueInfo,
			subjectInfo);

		// count hidden or available items

		if (subjectInfo.available) {

			countAvailable (
				queueInfo,
				subjectInfo);

		} else {

			countUnavailable (
				queueInfo,
				subjectInfo);

		}

	}

	private
	SubjectInfo createSubjectInfo (
			@NonNull QueueInfo queueInfo,
			@NonNull QueueSubjectRec subject) {

		// find next item

		QueueItemRec item =
			getNextItem (
				subject);

		// create subject info

		SubjectInfo subjectInfo =
			new SubjectInfo ();

		subjectInfo.subject = subject;
		subjectInfo.item = item;
		subjectInfo.createdTime = item.getCreatedTime ();
		subjectInfo.effectiveTime = item.getCreatedTime ();
		subjectInfo.priority = item.getPriority ();
		subjectInfo.state = item.getState ();

		// check preferred user

		subjectInfo.preferredUser =
			ifNull (
				subjectInfo.subject.getForcePreferredUser (),
				subjectInfo.subject.getPreferredUser ());

		subjectInfo.preferred =
			isNotNull (
				subjectInfo.preferredUser);

		subjectInfo.preferredByUs = (

			subjectInfo.preferred

			&& equal (
				subjectInfo.preferredUser,
				effectiveUser)

		);

		subjectInfo.preferredByOther = (

			subjectInfo.preferred

			&& notEqual (
				subjectInfo.preferredUser,
				effectiveUser)

		);

		if (subjectInfo.preferred) {

			UserPrivChecker preferredUserPrivChecker =
				userPrivCheckerBuilderProvider.get ()

				.userId (
					(long) (int)
					subjectInfo.preferredUser.getId ())

				.build ();

			subjectInfo.preferredByOverflowOperator = (

				preferredUserPrivChecker.canRecursive (
					queueInfo.queue,
					"reply_overflow")

				&& ! preferredUserPrivChecker.canSimple (
					queueInfo.queue,
					"reply")

			);

			subjectInfo.preferredByOwnOperator =
				! subjectInfo.preferredByOverflowOperator;

		} else {

			subjectInfo.preferredByOverflowOperator = false;
			subjectInfo.preferredByOwnOperator = false;

		}

		// extend effective time due to preferred user

		if (

			subjectInfo.preferredByOther

			&& (
				queueInfo.isOverflowUser
				|| subjectInfo.preferredByOwnOperator
			)

		) {

			subjectInfo.actualPreferredUserDelay =
				queueInfo.configuredPreferredUserDelay;

			subjectInfo.effectiveTime =
				subjectInfo.effectiveTime.plus (
					queueInfo.configuredPreferredUserDelay);

		}

		// extend effective time due to overflow user

		if (queueInfo.isOverflowUser) {

			if (queueInfo.ownOperatorsActive) {

				subjectInfo.overflowDelay =
					Duration.standardSeconds (
						ifNull (
							queueInfo.slice.getQueueOverflowGraceTime (),
							0l));

				subjectInfo.effectiveTime =
					subjectInfo.effectiveTime.plus (
						subjectInfo.overflowDelay);

			} else {

				subjectInfo.overflowDelay =
					Duration.standardSeconds (
						ifNull (
							queueInfo.slice.getQueueOverflowInactivityTime (),
							0l));

				subjectInfo.effectiveTime =
					subjectInfo.effectiveTime.plus (
						subjectInfo.overflowDelay);

			}

		}

		// check claimed user

		if (
			equal (
				subjectInfo.state (),
				QueueItemState.claimed)
		) {

			subjectInfo.claimed = true;

			subjectInfo.claimedByUser =
				subjectInfo.item.getQueueItemClaim ().getUser ();

			subjectInfo.available = false;

		} else {

			subjectInfo.claimed = false;

			subjectInfo.available =
				laterThan (
					transaction.now (),
					subjectInfo.effectiveTime);

		}

		// return

		return subjectInfo;

	}

	private
	QueueInfo createQueueInfo (
			@NonNull QueueRec queue) {

		QueueInfo queueInfo =
			new QueueInfo ();

		queueInfo.queue =
			queue;

		queueInfo.slice =
			optionalRequired (
				objectManager.getAncestor (
					SliceRec.class,
					queueInfo.queue));

		queueInfo.configuredPreferredUserDelay =
			queueManager.getPreferredUserDelay (
				queue);

		// check permissions

		queueInfo.canReplyExplicit =
			checkPrivExplicit (
				queue,
				"reply");

		queueInfo.canReplyImplicit =
			checkPrivImplicit (
				queue,
				"reply");

		queueInfo.canReplyOverflowExplicit =
			checkPrivExplicit (
				queue,
				"reply_overflow");

		queueInfo.canReplyOverflowImplicit =
			checkPrivImplicit (
				queue,
				"reply_overflow");

		// check special states

		queueInfo.isOverflowUser =
			queueInfo.canReplyOverflowImplicit
			&& ! queueInfo.canReplyExplicit;

		queueInfo.ownOperatorsActive =
			queueLogic.sliceHasQueueActivity (
				queueInfo.slice);

		// return

		return queueInfo;

	}

	private
	boolean checkPrivExplicit (
			@NonNull Record<?> parent,
			@NonNull String privCode) {

		if (
			! loggedInUserPrivChecker.canRecursive (
				parent,
				"reply")
		) {
			return false;
		}

		if (
			isNull (
				effectiveUser)
		) {

			return true;

		} else {

			return effectiveUserPrivChecker.canSimple (
				parent,
				privCode);

		}

	}

	private
	boolean checkPrivImplicit (
			@NonNull Record<?> parent,
			@NonNull String privCode) {

		if (
			! loggedInUserPrivChecker.canRecursive (
				parent,
				"reply")
		) {
			return false;
		}

		if (
			isNull (
				effectiveUser)
		) {

			return true;

		} else {

			return effectiveUserPrivChecker.canRecursive (
				parent,
				privCode);

		}

	}

	private
	void updateQueueInfo (
			@NonNull QueueInfo queueInfo,
			@NonNull SubjectInfo subjectInfo) {

		// check

		Instant createdTime =
			subjectInfo.createdTime;

		if (
			lessThan (
				subjectInfo.priority,
				queueInfo.highestPriority)
		) {

			queueInfo.highestPriority =
				subjectInfo.priority;

		}

		if (
			earlierThan (
				createdTime,
				queueInfo.oldest)
		) {

			queueInfo.oldest =
				createdTime;

		}

	}

	private
	QueueItemRec getNextItem (
			@NonNull QueueSubjectRec subject) {

		long nextItemIndex =
			+ subject.getTotalItems ()
			- subject.getActiveItems ();

		QueueItemRec item =
			queueItemHelper.findByIndex (
				subject,
				nextItemIndex);

		if (
			notIn (
				item.getState (),
				QueueItemState.pending,
				QueueItemState.claimed)
		) {

			throw new RuntimeException (
				stringFormat (
					"Queue item %s in invalid state %s",
					item.getId (),
					item.getState ()));

		}

		return item;

	}

	private
	void countTotal (
			@NonNull QueueInfo queueInfo,
			@NonNull SubjectInfo subjectInfo) {

		result.totalItems +=
			subjectInfo.subject.getActiveItems ();

		queueInfo.totalItems +=
			subjectInfo.subject.getActiveItems ();

	}

	private
	void countWaiting (
			@NonNull QueueInfo queueInfo,
			@NonNull SubjectInfo subjectInfo) {

		result.waitingItems +=
			+ subjectInfo.subject.getActiveItems ()
			- 1;

		queueInfo.waitingItems +=
			+ subjectInfo.subject.getActiveItems ()
			- 1;

		if (
			earlierThan (
				subjectInfo.createdTime,
				queueInfo.oldestWaiting)
		) {

			queueInfo.oldestWaiting =
				subjectInfo.createdTime;

		}

		if (
			lessThan (
				subjectInfo.priority,
				queueInfo.highestPriorityWaiting)
		) {

			queueInfo.highestPriorityWaiting =
				subjectInfo.priority;

		}

	}

	private
	void countClaimed (
			@NonNull QueueInfo queueInfo,
			@NonNull SubjectInfo subjectInfo) {

		result.totalClaimedSubjects ++;
		queueInfo.claimedSubjects ++;

		result.totalClaimedItems ++;
		queueInfo.claimedItems ++;

		if (
			effectiveUser != null
			&& equal (
				effectiveUser,
				subjectInfo.item.getQueueItemClaim ().getUser ())
		) {

			result.userClaimedItems ++;
			queueInfo.userClaimedItems ++;

		}

		if (
			earlierThan (
				subjectInfo.createdTime,
				queueInfo.oldestClaimed)
		) {

			queueInfo.oldestClaimed =
				subjectInfo.createdTime;

		}

		if (
			lessThan (
				subjectInfo.priority (),
				queueInfo.highestPriorityClaimed)
		) {

			queueInfo.highestPriorityClaimed =
				subjectInfo.priority;

		}

	}

	private
	void countUnavailable (
			@NonNull QueueInfo queueInfo,
			@NonNull SubjectInfo subjectInfo) {

		result.totalUnavailableSubjects ++;
		result.totalUnavailableItems ++;

		queueInfo.totalUnavailableSubjects ++;
		queueInfo.totalUnavailableItems ++;

		if (
			earlierThan (
				subjectInfo.effectiveTime,
				queueInfo.oldestUnavailable)
		) {

			queueInfo.oldestUnavailable =
				subjectInfo.effectiveTime;

		}

	}

	private
	void countAvailable (
			@NonNull QueueInfo queueInfo,
			@NonNull SubjectInfo subjectInfo) {

		result.totalAvailableSubjects ++;
		queueInfo.availableSubjects ++;

		result.totalAvailableItems ++;
		queueInfo.availableItems ++;

		if (
			earlierThan (
				subjectInfo.effectiveTime,
				queueInfo.oldestAvailable)
		) {

			queueInfo.oldestAvailable =
				subjectInfo.effectiveTime;

		}

		if (
			lessThan (
				subjectInfo.priority,
				queueInfo.highestPriorityAvailable)
		) {

			queueInfo.highestPriorityAvailable =
				subjectInfo.priority;

		}

	}

	@Accessors (fluent = true)
	@Data
	public static
	class QueueInfo {

		QueueRec queue;
		SliceRec slice;

		Duration configuredPreferredUserDelay;

		List<SubjectInfo> subjectInfos =
			new ArrayList<> ();

		// all

		long highestPriority =
			Long.MAX_VALUE;

		Instant oldest =
			new Instant (
				Long.MAX_VALUE);

		// waiting

		long waitingItems = 0;

		long highestPriorityWaiting =
			Long.MAX_VALUE;

		Instant oldestWaiting =
			new Instant (
				Long.MAX_VALUE);

		// available

		long availableItems = 0;
		long availableSubjects = 0;

		long highestPriorityAvailable =
			Long.MAX_VALUE;

		Instant oldestAvailable =
			new Instant (
				Long.MAX_VALUE);

		// claimed

		long highestPriorityClaimed =
			Long.MAX_VALUE;

		Instant oldestClaimed =
			new Instant (
				Long.MAX_VALUE);

		// preferred

		Instant oldestUnavailable =
			new Instant (
				Long.MAX_VALUE);

		// counts

		long totalItems = 0;
		long claimedItems = 0;
		long userClaimedItems = 0;
		long myClaimedItems = 0;
		long claimedSubjects = 0;
		long totalUnavailableItems = 0;
		long totalUnavailableSubjects = 0;

		// permissions

		boolean canReplyExplicit;
		boolean canReplyImplicit;
		boolean canReplyOverflowExplicit;
		boolean canReplyOverflowImplicit;

		// other state

		boolean isOverflowUser;
		boolean ownOperatorsActive;

		// comparators

		public final static
		Comparator<QueueInfo> oldestAvailableComparator =
			new Comparator<QueueInfo> () {

			@Override
			public
			int compare (
					@NonNull QueueInfo left,
					@NonNull QueueInfo right) {

				return new CompareToBuilder ()

					.append (
						left.highestPriorityAvailable,
						right.highestPriorityAvailable)

					.append (
						left.oldestAvailable,
						right.oldestAvailable)

					.append (
						left.queue,
						right.queue)

					.toComparison ();

			}

		};

		public final static
		Comparator<QueueInfo> oldestComparator =
			new Comparator<QueueInfo> () {

			@Override
			public
			int compare (
					QueueInfo left,
					QueueInfo right) {

				return new CompareToBuilder ()

					.append (
						left.highestPriority,
						right.highestPriority)

					.append (
						left.oldest,
						right.oldest)

					.append (
						left.queue,
						right.queue)

					.toComparison ();

			}

		};

	}

	@Accessors (fluent = true)
	@Data
	public static
	class SubjectInfo {

		QueueSubjectRec subject;
		QueueItemRec item;

		Instant createdTime;
		Instant effectiveTime;
		Long priority;

		QueueItemState state;

		boolean preferred;
		UserRec preferredUser;
		boolean preferredByOther;
		boolean preferredByUs;
		boolean preferredByOwnOperator;
		boolean preferredByOverflowOperator;
		Duration actualPreferredUserDelay;

		boolean claimed;
		UserRec claimedByUser;

		Duration overflowDelay;

		boolean available;

		public final static
		Comparator<SubjectInfo> effectiveTimeComparator =
			new Comparator<SubjectInfo> () {

			@Override
			public
			int compare (
					SubjectInfo left,
					SubjectInfo right) {

				return new CompareToBuilder ()

					.append (
						left.priority,
						right.priority)

					.append (
						left.effectiveTime,
						right.effectiveTime)

					.append (
						left.subject,
						right.subject)

					.toComparison ();

			}

		};

		public final static
		Comparator<SubjectInfo> createdTimeComparator =
			new Comparator<SubjectInfo> () {

			@Override
			public
			int compare (
					SubjectInfo left,
					SubjectInfo right) {

				return new CompareToBuilder ()

					.append (
						left.item.getCreatedTime (),
						right.item.getCreatedTime ())

					.append (
						left.subject,
						right.subject)

					.toComparison ();

			}

		};

	}

}
