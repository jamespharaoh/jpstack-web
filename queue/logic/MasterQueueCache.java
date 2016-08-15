package wbs.platform.queue.logic;

import static wbs.framework.utils.etc.Misc.isNotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.queue.model.QueueItemObjectHelper;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueItemState;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.queue.model.QueueSubjectObjectHelper;
import wbs.platform.queue.model.QueueSubjectRec;

@PrototypeComponent ("masterQueueCache")
public
class MasterQueueCache
	implements QueueCache {

	// dependencies

	@Inject
	QueueItemObjectHelper queueItemHelper;

	@Inject
	QueueSubjectObjectHelper queueSubjectHelper;

	// state

	Map<Pair<Long,Long>,QueueItemRec> queueItemsBySubjectAndIndex;

	List<QueueSubjectRec> queueSubjects;

	Map<Long,List<QueueSubjectRec>> queueSubjectsByQueue;

	// lifecycle

	@PostConstruct
	public
	void setup () {

		// create queue item index
	
		queueItemsBySubjectAndIndex =
			ImmutableMap.copyOf (

			queueItemHelper.find (
				ImmutableList.of (
					QueueItemState.pending,
					QueueItemState.claimed))
	
			.stream ()
	
			.filter (
				queueItem ->
					isNotNull (
						queueItem.getQueueSubject ()))
	
			.collect (
				Collectors.toMap (
	
				queueItem ->
					Pair.of (
						(long) queueItem.getQueueSubject ().getId (),
						(long) queueItem.getIndex ()),
	
				queueItem ->
					queueItem)
	
			)

		);

		// create queue subject indexes

		queueSubjects =
			ImmutableList.copyOf (

			queueSubjectHelper.findActive ()

		);

		queueSubjectsByQueue =
			ImmutableMap.copyOf (

			queueSubjects.stream ()

			.collect (
				Collectors.groupingBy (
					queueSubject ->
						(long) queueSubject.getQueue ().getId ()))

		);

	}

	// implementation

	@Override
	public
	QueueItemRec findQueueItemByIndex (
			@NonNull QueueSubjectRec subject,
			@NonNull Long index) {

		return queueItemsBySubjectAndIndex.get (
			Pair.of (
				(long) subject.getId (),
				index));

	}

	@Override
	public
	List<QueueSubjectRec> findQueueSubjects () {

		return queueSubjects;

	}

	@Override
	public
	List<QueueSubjectRec> findQueueSubjects (
			@NonNull QueueRec queue) {

		return queueSubjectsByQueue.get (
			(long) queue.getId ());

	}

}
