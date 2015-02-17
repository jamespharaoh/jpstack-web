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
import wbs.platform.queue.model.QueueRec;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.scaffold.model.SliceRec;

import com.google.common.base.Optional;

@PrototypeComponent ("queueStatsFilter")
public
class QueueStatsFilter {

	// dependencies

	@Inject
	ObjectManager objectManager;

	@Inject
	SliceObjectHelper sliceHelper;

	// state

	Optional<SliceRec> slice;

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
