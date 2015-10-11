package wbs.platform.queue.hibernate;

import java.util.List;

import wbs.framework.hibernate.HibernateDao;
import wbs.framework.record.Record;
import wbs.platform.queue.model.QueueRec;
import wbs.platform.queue.model.QueueSubjectDaoMethods;
import wbs.platform.queue.model.QueueSubjectRec;

public
class QueueSubjectDaoHibernate
	extends HibernateDao
	implements QueueSubjectDaoMethods {

	@Override
	public
	List<QueueSubjectRec> findActive (
			QueueRec queue) {

		return findMany (
			QueueSubjectRec.class,

			createQuery (
				"FROM QueueSubjectRec queueSubject " +
				"WHERE queueSubject.activeItems > 0 " +
					"AND queueSubject.queue.id = :queueId")

			.setInteger (
				"queueId",
				queue.getId ())

			.list ());

	}

	@Override
	public
	QueueSubjectRec find (
			QueueRec queue,
			Record<?> object) {

		return findOne (
			QueueSubjectRec.class,

			createQuery (
				"FROM QueueSubjectRec queueSubject " +
				"WHERE queueSubject.queue.id = :queueId " +
					"AND queueSubject.objectId = :objectId")

			.setInteger (
				"queueId",
				queue.getId ())

			.setInteger (
				"objectId",
				object.getId ())

			.list ());

	}

	@Override
	public
	List<QueueSubjectRec> findActive () {

		return findMany (
			QueueSubjectRec.class,

			createQuery (
				"FROM QueueSubjectRec queueSubject " +
				"WHERE queueSubject.activeItems > 0")

			.list ());

	}

}
