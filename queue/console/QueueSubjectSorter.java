package wbs.platform.queue.console;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.notIn;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.console.priv.PrivChecker;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.queue.model.QueueItemObjectHelper;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueItemState;
import wbs.platform.queue.model.QueueObjectHelper;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.queue.model.QueueSubjectObjectHelper;
import wbs.platform.queue.model.QueueSubjectRec;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.model.UserRec;

@Accessors (fluent = true)
@PrototypeComponent ("queueSubjectSorter")
public
class QueueSubjectSorter {

	@Inject
	PrivChecker privChecker;

	@Inject
	QueueObjectHelper queueHelper;

	@Inject
	QueueItemObjectHelper queueItemHelper;

	@Inject
	QueueManager queueManager;

	@Inject
	QueueSubjectObjectHelper queueSubjectHelper;

	@Inject
	UserConsoleHelper userHelper;

	// inputs

	@Setter
	QueueRec queue;

	@Setter
	UserRec user;

	// outputs

	@Getter
	List<SubjectInfo> subjects;

	@Getter
	List<QueueInfo> queues;

	@Getter
	int totalItems = 0;

	@Getter
	int waitingItems = 0;

	@Getter
	int availableItems = 0;

	@Getter
	int availableSubjects = 0;

	@Getter
	int claimedItems = 0;

	@Getter
	int userClaimedItems = 0;

	@Getter
	int claimedSubjects = 0;

	@Getter
	int preferredItems = 0;

	@Getter
	int preferredSubjects = 0;

	public
	QueueSubjectSorter sort () {

		long now =
			System.currentTimeMillis ();

		List<QueueSubjectRec> queueSubjects =
			queue != null
				? queueSubjectHelper.findActive (queue)
				: queueSubjectHelper.findActive ();

		Set<SubjectInfo> allSubjectInfos =
			new HashSet<SubjectInfo> ();

		Map<QueueRec,QueueInfo> allQueueInfos =
			new HashMap<QueueRec,QueueInfo> ();

		// pre-fetch interesting data

/*
		queueHelper.find (
			queueSubjects.stream ()
				.map (QueueSubjectRec::getId)
				.map (value -> (long) (int) value)
				.collect (Collectors.toSet ())
				.stream ()
				.collect (Collectors.toList ()));

		Map<Pair<Long,Long>,QueueItemRec> queueItemsByIndex =
			queueItemHelper.findActive ().stream ()

			.filter (
				queueItem -> queueItem.getQueueSubject () != null)

			.collect (
				Collectors.<QueueItemRec,Pair<Long,Long>,QueueItemRec>toMap (
					queueItem -> Pair.of (
						(long) (int) queueItem.getQueueSubject ().getId (),
						(long) (int) queueItem.getIndex ()),
					queueItem -> queueItem));

		userHelper.findAll ();

*/

		for (
			QueueSubjectRec subject
				: queueSubjects
		) {

			QueueRec queue =
				subject.getQueue ();

			// check permissions

			if (! privChecker.can (queue, "reply"))
				continue;

			// find or create queue info

			QueueInfo queueInfo =
				allQueueInfos.get (
					queue);

			if (queueInfo == null) {

				queueInfo = new QueueInfo ();
				queueInfo.queue = queue;

				queueInfo.delay =
					queueManager.getPreferredUserDelay (
						queue);

				allQueueInfos.put (
					queue,
					queueInfo);

			}

			// count total items

			totalItems +=
				subject.getActiveItems ();

			queueInfo.totalItems +=
				subject.getActiveItems ();

			// find next item

			long nextItemIndex =
				+ subject.getTotalItems ()
				- subject.getActiveItems ();

/*
			QueueItemRec item =
				queueItemsByIndex.get (
					Pair.of (
						(long) (int) subject.getId (),
						(long) (int) nextItemIndex));
*/

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

			long createdTime =
				item.getCreatedTime ().getTime ();

			if (createdTime < queueInfo.oldest)
				queueInfo.oldest = createdTime;

			// create subject info

			SubjectInfo subjectInfo =
				new SubjectInfo ();

			subjectInfo.subject = subject;
			subjectInfo.item = item;
			subjectInfo.effectiveTime = createdTime;

			allSubjectInfos.add (subjectInfo);

			// count waiting items

			waitingItems +=
				+ subject.getActiveItems ()
				- 1;

			queueInfo.waitingItems +=
				+ subject.getActiveItems ()
				- 1;

			if (createdTime < queueInfo.oldestWaiting)
				queueInfo.oldestWaiting = createdTime;

			// if the item is claimed then the subject is not available to the
			// user

			if (item.getState () == QueueItemState.claimed) {

				claimedSubjects ++;
				queueInfo.claimedSubjects ++;

				claimedItems ++;
				queueInfo.claimedItems ++;

				if (
					user != null
					&& equal (
						user,
						item.getQueueItemClaim ().getUser ())
				) {

					userClaimedItems ++;
					queueInfo.userClaimedItems ++;

				}

				if (createdTime < queueInfo.oldestClaimed)
					queueInfo.oldestClaimed = createdTime;

				subjectInfo.claimed = true;

				continue;

			}

			// preferred items have their effective time delayed

			UserRec preferredUser =
				ifNull (
					subject.getForcePreferredUser (),
					subject.getPreferredUser ());

			boolean preferred = (

				isNotNull (
					preferredUser)

				&& notEqual (
					preferredUser,
					user)

			);

			if (preferred) {

				subjectInfo.effectiveTime +=
					queueInfo.delay;

			}

			// work out if we should hide because of being preferred

			if (preferred) {

				long preferredTime =
					+ item.getPendingTime ().getTime ()
					+ queueInfo.delay;

				if (now < preferredTime)
					subjectInfo.hiddenPreferred = true;

			}

			// count hidden preferred items

			if (subjectInfo.hiddenPreferred) {

				preferredSubjects ++;
				preferredItems ++;

				queueInfo.preferredSubjects ++;
				queueInfo.preferredItems ++;

				if (createdTime < queueInfo.oldestPreferred)
					queueInfo.oldestPreferred = createdTime;

				continue;

			}

			// count available items

			availableSubjects ++;
			queueInfo.availableSubjects ++;

			availableItems ++;
			queueInfo.availableItems ++;

			if (createdTime < queueInfo.oldestAvailable)
				queueInfo.oldestAvailable = createdTime;

			subjectInfo.available = true;

		}

		// convert subjects to list, filter and sort

		subjects =
			new ArrayList<SubjectInfo> (
				allSubjectInfos.size ());

		for (
			SubjectInfo subjectInfo
				: allSubjectInfos
		) {

			if (user != null && ! subjectInfo.available)
				continue;

			subjects.add (subjectInfo);

		}

		Collections.sort (
			subjects,
			user != null
				? SubjectInfo.effectiveTimeComparator
				: SubjectInfo.oldestComparator);

		// convert queues to list, filter and sort

		queues =
			new ArrayList<QueueInfo> (
				allQueueInfos.size ());

		for (
			QueueInfo queueInfo
				: allQueueInfos.values ()
		) {

			if (user != null && queueInfo.availableItems == 0)
				continue;

			queues.add (queueInfo);

		}

		Collections.sort (
			queues,
			user != null
				? QueueInfo.oldestAvailableComparator
				: QueueInfo.oldestComparator);

		// and return

		return this;

	}

	@Accessors (fluent = true)
	public static
	class QueueInfo {

		@Getter
		QueueRec queue;

		@Getter
		long delay;

		@Getter
		int totalItems = 0;

		@Getter
		long oldest = Long.MAX_VALUE;

		@Getter
		int waitingItems = 0;

		@Getter
		long oldestWaiting = Long.MAX_VALUE;

		@Getter
		int availableItems = 0;

		@Getter
		int availableSubjects = 0;

		@Getter
		long oldestAvailable = Long.MAX_VALUE;

		@Getter
		int claimedItems = 0;

		@Getter
		int userClaimedItems = 0;

		@Getter
		int myClaimedItems = 0;

		@Getter
		int claimedSubjects = 0;

		@Getter
		long oldestClaimed = Long.MAX_VALUE;

		@Getter
		int preferredItems = 0;

		@Getter
		int preferredSubjects = 0;

		@Getter
		long oldestPreferred = Long.MAX_VALUE;

		public final static
		Comparator<QueueInfo> oldestAvailableComparator =
			new Comparator<QueueInfo> () {

			@Override
			public
			int compare (
					QueueInfo left,
					QueueInfo right) {

				return new CompareToBuilder ()
					.append (left.oldestAvailable, right.oldestAvailable)
					.append (left.queue, right.queue)
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
					.append (left.oldest, right.oldest)
					.append (left.queue, right.queue)
					.toComparison ();

			}

		};

	}

	@Accessors (fluent = true)
	public static
	class SubjectInfo {

		@Getter
		QueueSubjectRec subject;

		@Getter
		QueueItemRec item;

		@Getter
		long effectiveTime;

		@Getter
		boolean claimed;

		@Getter
		boolean available;

		@Getter
		boolean hiddenPreferred;

		public final static
		Comparator<SubjectInfo> effectiveTimeComparator =
			new Comparator<SubjectInfo> () {

			@Override
			public
			int compare (
					SubjectInfo left,
					SubjectInfo right) {

				return new CompareToBuilder ()
					.append (left.effectiveTime, right.effectiveTime)
					.append (left.subject, right.subject)
					.toComparison ();

			}

		};

		public final static
		Comparator<SubjectInfo> oldestComparator =
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
					.append (left.subject, right.subject)
					.toComparison ();

			}

		};

	}

}
