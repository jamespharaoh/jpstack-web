package wbs.platform.queue.hibernate;

import java.util.List;

import lombok.NonNull;
import wbs.framework.hibernate.HibernateDao;
import wbs.platform.queue.model.QueueTypeDao;
import wbs.platform.queue.model.QueueTypeRec;

public
class QueueTypeDaoHibernate
	extends HibernateDao
	implements QueueTypeDao {

	@Override
	public
	List<QueueTypeRec> findAll () {

		return findMany (
			"findAll ()",
			QueueTypeRec.class,

			createCriteria (
				QueueTypeRec.class)

		);

	}

	@Override
	public
	QueueTypeRec findRequired (
			@NonNull Long queueTypeId) {

		return get (
			QueueTypeRec.class,
			queueTypeId);

	}

}
