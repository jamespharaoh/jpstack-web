package wbs.platform.queue.console;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectManager;

import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueObjectHelper;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.scaffold.model.SliceRec;

@PrototypeComponent ("queueStatsFilter")
public
class QueueStatsFilter {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	QueueObjectHelper queueHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	// state

	Optional <SliceRec> slice;
	Optional <Set <QueueRec>> queues;

	Set <QueueRec> includeQueues =
		new HashSet<QueueRec> ();

	Set <QueueRec> excludeQueues =
		new HashSet <QueueRec> ();

	// implementation

	public
	void conditions (
			@NonNull Transaction parentTransaction,
			@NonNull Map <String, Object> conditions) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"conditions");

		) {

			if (conditions.containsKey ("sliceId")) {

				slice =
					Optional.of (
						sliceHelper.findRequired (
							transaction,
							(Long)
							conditions.get (
								"sliceId")));

			} else {

				slice =
					Optional.absent ();

			}

			if (conditions.containsKey ("queueId")) {

				ImmutableSet.Builder <QueueRec> queuesBuilder =
					ImmutableSet.builder ();

				Set <?> queueIds =
					(Set <?>)
					conditions.get (
						"queueId");

				for (
					Object queueId
						: queueIds
				) {

					queuesBuilder.add (
						queueHelper.findRequired (
							transaction,
							(Long)
							queueId));

				}

				queues =
					Optional.of (
						queuesBuilder.build ());

			} else {

				queues =
					optionalAbsent ();

			}

		}

	}

	public
	boolean filterQueue (
			@NonNull Transaction parentTransaction,
			@NonNull QueueRec queue) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"filterQueue");

		) {

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
					transaction,
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

	}

	public
	List <QueueItemRec> filterQueueItems (
			@NonNull Transaction parentTransaction,
			@NonNull List <QueueItemRec> allQueueItems) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"filterQueueItems");

		) {

			List <QueueItemRec> filteredQueueItems =
				new ArrayList<> ();

			for (
				QueueItemRec queueItem
					: allQueueItems
			) {

				QueueRec queue =
					queueItem.getQueueSubject ().getQueue ();

				if (
					! filterQueue (
						transaction,
						queue)
				) {
					continue;
				}

				filteredQueueItems.add (
					queueItem);

			}

			return filteredQueueItems;

		}

	}

}
