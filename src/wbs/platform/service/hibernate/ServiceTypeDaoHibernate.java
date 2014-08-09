package wbs.platform.service.hibernate;

import java.util.List;

import wbs.framework.hibernate.HibernateDao;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.platform.service.model.ServiceTypeDao;
import wbs.platform.service.model.ServiceTypeRec;

public
class ServiceTypeDaoHibernate
	extends HibernateDao
	implements ServiceTypeDao {

	@Override
	public
	List<ServiceTypeRec> findByParentObjectType (
			ObjectTypeRec parentObjectType) {

		return findMany (
			ServiceTypeRec.class,

			createQuery (
				"FROM ServiceTypeRec serviceType " +
				"WHERE serviceType.parentObjectType = :parentObjectType")

			.setEntity (
				"parentObjectType",
				parentObjectType)

			.list ());

	}

}
