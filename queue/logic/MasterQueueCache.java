package wbs.platform.queue.logic;

import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.etc.Misc.isNotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;

import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

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

	// TODO this is probably not needed with generic caching layer

	// dependencies

	@SingletonDependency
	QueueItemObjectHelper queueItemHelper;

	@SingletonDependency
	QueueSubjectObjectHelper queueSubjectHelper;

	// state

	Map <Pair <Long, Long>, QueueItemRec> queueItemsBySubjectAndIndex;

	List <QueueSubjectRec> queueSubjects;

	Map <Long, List <QueueSubjectRec>> queueSubjectsByQueue;

	// lifecycle

	@NormalLifecycleSetup
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
						queueItem.getQueueSubject ().getId (),
						queueItem.getIndex ()),

				queueItem ->
					queueItem)

			)

		);

		// create queue subject indexes

		queueSubjects =
			ImmutableList.copyOf (
				queueSubjectHelper.findActive ());

		queueSubjectsByQueue =
			ImmutableMap.copyOf (
				queueSubjects.stream ()

			.collect (
				Collectors.groupingBy (
					queueSubject ->
						queueSubject.getQueue ().getId ()))

		);

	}

	// implementation

	@Override
	public
	QueueItemRec findQueueItemByIndexRequired (
			@NonNull QueueSubjectRec subject,
			@NonNull Long index) {

		return mapItemForKeyRequired (
			queueItemsBySubjectAndIndex,
			Pair.of (
				subject.getId (),
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
			queue.getId ());

	}

}
