package wbs.platform.queue.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.entity.record.Record;
import wbs.framework.hibernate.HibernateDao;
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
			@NonNull QueueRec queue) {

		return findMany (
			"findActive (queue)",
			QueueSubjectRec.class,

			createCriteria (
				QueueSubjectRec.class,
				"_queueSubject")

			.add (
				Restrictions.gt (
					"_queueSubject.activeItems",
					0l))

			.add (
				Restrictions.eq (
					"_queueSubject.queue",
					queue))

		);

	}

	@Override
	public
	QueueSubjectRec find (
			@NonNull QueueRec queue,
			@NonNull Record<?> object) {

		return findOneOrNull (
			"find (queue, object)",
			QueueSubjectRec.class,

			createCriteria (
				QueueSubjectRec.class,
				"_queueSubject")

			.add (
				Restrictions.eq (
					"_queueSubject.queue",
					queue))

			.add (
				Restrictions.eq (
					"_queueSubject.objectId",
					object.getId ()))

		);

	}

	@Override
	public
	List<QueueSubjectRec> findActive () {

		return findMany (
			"findActive ()",
			QueueSubjectRec.class,

			createCriteria (
				QueueSubjectRec.class,
				"_queueSubject")

			.add (
				Restrictions.gt (
					"_queueSubject.activeItems",
					0l))

		);

	}

}
