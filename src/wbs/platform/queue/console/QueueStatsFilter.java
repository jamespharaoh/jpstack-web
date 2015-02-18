package wbs.platform.queue.console;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.object.ObjectManager;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueObjectHelper;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.scaffold.model.SliceRec;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

@PrototypeComponent ("queueStatsFilter")
public
class QueueStatsFilter {

	// dependencies

	@Inject
	ObjectManager objectManager;

	@Inject
	QueueObjectHelper queueHelper;

	@Inject
	SliceObjectHelper sliceHelper;

	// state

	Optional<SliceRec> slice;
	Optional<Set<QueueRec>> queues;

	Set<QueueRec> includeQueues =
		new HashSet<QueueRec> ();

	Set<QueueRec> excludeQueues =
		new HashSet<QueueRec> ();

	// implementation

	public
	void conditions (
			Map<String,Object> conditions) {

		if (conditions.containsKey ("sliceId")) {

			slice =
				Optional.of (
					sliceHelper.find (
						(Integer)
						conditions.get ("sliceId")));

		} else {

			slice =
				Optional.<SliceRec>absent ();

		}

		if (conditions.containsKey ("queueId")) {

			ImmutableSet.Builder<QueueRec> queuesBuilder =
				ImmutableSet.<QueueRec>builder ();

			Set<?> queueIds =
				(Set<?>)
				conditions.get ("queueId");

			for (
				Object queueId
					: queueIds
			) {

				queuesBuilder.add (
					queueHelper.find (
						(Integer)
						queueId));

			}

			queues =
				Optional.<Set<QueueRec>>of (
					queuesBuilder.build ());

		} else {

			queues =
				Optional.<Set<QueueRec>>absent ();

		}

	}

	public
	boolean filterQueue (
			QueueRec queue) {

		if (
			includeQueues.contains (
				queue)
		) {
			return true;
		}

		if (
			excludeQueues.contains (
				queue)
		) {
			return false;
		}

		boolean exclude = false;

		if (

			slice.isPresent ()

			&& ! objectManager.isParent (
				queue,
				slice.get ())

		) {
			exclude = true;
		}

		if (

			queues.isPresent ()

			&& ! queues.get ().contains (
				queue)

		) {
			exclude = true;
		}

		if (exclude) {

			excludeQueues.add (
				queue);

			return false;

		} else {

			includeQueues.add (
				queue);

			return true;

		}

	}

	public
	List<QueueItemRec> filterQueueItems (
			List<QueueItemRec> allQueueItems) {

		List<QueueItemRec> filteredQueueItems =
			new ArrayList<QueueItemRec> ();

		for (
			QueueItemRec queueItem
				: allQueueItems
		) {

			QueueRec queue =
				queueItem.getQueueSubject ().getQueue ();

			if (! filterQueue (queue))
				continue;

			filteredQueueItems.add (
				queueItem);

		}

		return filteredQueueItems;

	}

}
