package wbs.platform.queue.logic;

import java.util.List;

import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.queue.model.QueueSubjectRec;

public
interface QueueCache {

	QueueItemRec findQueueItemByIndex (
			QueueSubjectRec subject,
			Long index);

	List<QueueSubjectRec> findQueueSubjects ();

	List<QueueSubjectRec> findQueueSubjects (
			QueueRec queue);

}
