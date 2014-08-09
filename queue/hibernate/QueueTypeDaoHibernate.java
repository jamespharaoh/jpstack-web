package wbs.platform.queue.hibernate;

import java.util.List;

import wbs.framework.hibernate.HibernateDao;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.platform.queue.model.QueueTypeDao;
import wbs.platform.queue.model.QueueTypeRec;

public
class QueueTypeDaoHibernate
	extends HibernateDao
	implements QueueTypeDao {

	@Override
	public
	List<QueueTypeRec> findByParentObjectType (
			ObjectTypeRec parentObjectType) {

		return findMany (
			QueueTypeRec.class,

			createQuery (
				"FROM QueueTypeRec queueType " +
				"WHERE queueType.parentObjectType = :parentObjectType")

			.setEntity (
				"parentObjectType",
				parentObjectType)

			.list ());

	}

}
