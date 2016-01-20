package wbs.platform.queue.console;

import javax.inject.Inject;

import lombok.NonNull;

import com.google.common.collect.ImmutableList;

import wbs.console.helper.ConsoleHooks;
import wbs.console.priv.PrivChecker;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.object.ObjectManager;
import wbs.framework.record.Record;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueItemSearch;
import wbs.platform.queue.model.QueueRec;

@SingletonComponent ("queueItemConsoleHooks")
public
class QueueItemConsoleHooks
	implements ConsoleHooks<QueueItemRec> {

	// dependencies

	@Inject
	ObjectManager objectManager;

	@Inject
	PrivChecker privChecker;

	@Inject
	QueueConsoleHelper queueHelper;

	// implementation

	@Override
	public
	void applySearchFilter (
			@NonNull Object searchObject) {

		QueueItemSearch search =
			(QueueItemSearch)
			searchObject;

		search

			.filter (
				true);

		// queues

		ImmutableList.Builder<Integer> queuesBuilder =
			ImmutableList.<Integer>builder ();

		for (
			QueueRec queue
				: queueHelper.findAll ()
		) {

			Record<?> queueParent =
				objectManager.getParent (
					queue);

			 if (
			 	! privChecker.can (
			 		queueParent,
			 		"manage")
			 ) {
			 	continue;
			 }

			queuesBuilder.add (
				queue.getId ());

		}

		search

			.filterQueueIds (
				queuesBuilder.build ());

	}

}
