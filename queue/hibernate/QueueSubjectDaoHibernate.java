package wbs.platform.queue.hibernate;

import java.util.List;

import wbs.framework.hibernate.HibernateDao;
import wbs.platform.queue.model.QueueSubjectRec;
import wbs.platform.queue.model.QueueSubjectRec.QueueSubjectDaoMethods;

public
class QueueSubjectDaoHibernate
	extends HibernateDao
	implements QueueSubjectDaoMethods {

	@Override
	public
	List<QueueSubjectRec> findActiveByQueue (
			int queueId) {

		return findMany (
			QueueSubjectRec.class,

			createQuery (
				"FROM QueueSubjectRec queueSubject " +
				"WHERE queueSubject.activeItems > 0 " +
					"AND queueSubject.queue.id = :queueId")

			.setInteger (
				"queueId",
				queueId)

			.list ());

	}

	@Override
	public
	QueueSubjectRec findByQueueAndObject (
			int queueId,
			int objectId) {

		return findOne (
			QueueSubjectRec.class,

			createQuery (
				"FROM QueueSubjectRec queueSubject " +
				"WHERE queueSubject.queue.id = :queueId " +
					"AND queueSubject.objectId = :objectId")

			.setInteger (
				"queueId",
				queueId)

			.setInteger (
				"objectId",
				objectId)

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
