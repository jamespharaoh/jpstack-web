package wbs.platform.queue.hibernate;

import java.util.List;

import lombok.NonNull;

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
			@NonNull ObjectTypeRec parentType) {

		return findMany (
			QueueTypeRec.class,

			createQuery (
				"FROM QueueTypeRec queueType " +
				"WHERE queueType.parentType = :parentType")

			.setEntity (
				"parentType",
				parentType)

			.list ());

	}

}
