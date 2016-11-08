package wbs.platform.queue.console;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import wbs.console.helper.core.ConsoleHooks;
import wbs.console.priv.UserPrivChecker;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;
import wbs.framework.object.ObjectManager;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueItemSearch;
import wbs.platform.queue.model.QueueRec;

@SingletonComponent ("queueItemConsoleHooks")
public
class QueueItemConsoleHooks
	implements ConsoleHooks <QueueItemRec> {

	// singleton dependencies

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
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

		ImmutableList.Builder<Long> queuesBuilder =
			ImmutableList.builder ();

		for (
			QueueRec queue
				: queueHelper.findAll ()
		) {

			Record<?> queueParent =
				objectManager.getParentOrNull (
					queue);

			 if (
			 	! privChecker.canRecursive (
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
