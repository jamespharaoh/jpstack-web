package wbs.platform.queue.logic;

import java.util.List;

import javax.inject.Inject;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.platform.queue.model.QueueItemObjectHelper;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.queue.model.QueueSubjectObjectHelper;
import wbs.platform.queue.model.QueueSubjectRec;

@SingletonComponent ("dummyQueueCache")
public
class DummyQueueCache
	implements QueueCache {

	// dependencies

	@Inject
	QueueItemObjectHelper queueItemHelper;

	@Inject
	QueueSubjectObjectHelper queueSubjectHelper;

	// implementation

	@Override
	public
	QueueItemRec findQueueItemByIndex (
			@NonNull QueueSubjectRec subject,
			@NonNull Long index) {

		return queueItemHelper.findByIndexOrNull (
			subject,
			index);

	}

	@Override
	public
	List <QueueSubjectRec> findQueueSubjects () {

		return queueSubjectHelper.findActive ();

	}

	@Override
	public
	List <QueueSubjectRec> findQueueSubjects (
			@NonNull QueueRec queue) {

		return queueSubjectHelper.findActive (
			queue);

	}

}
