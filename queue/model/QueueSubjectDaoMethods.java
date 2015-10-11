package wbs.platform.queue.model;

import java.util.List;

import wbs.framework.record.Record;

public
interface QueueSubjectDaoMethods {

	QueueSubjectRec find (
			QueueRec queue,
			Record<?> object);

	List<QueueSubjectRec> findActive ();

	List<QueueSubjectRec> findActive (
			QueueRec queue);

}