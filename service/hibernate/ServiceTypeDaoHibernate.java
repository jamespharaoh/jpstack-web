package wbs.platform.service.hibernate;

import java.util.List;

import lombok.NonNull;

import wbs.framework.hibernate.HibernateDao;
import wbs.platform.service.model.ServiceTypeDao;
import wbs.platform.service.model.ServiceTypeRec;

public
class ServiceTypeDaoHibernate
	extends HibernateDao
	implements ServiceTypeDao {

	@Override
	public
	List<ServiceTypeRec> findAll () {

		return findMany (
			"findAll ()",
			ServiceTypeRec.class,

			createCriteria (
				ServiceTypeRec.class)

		);

	}

	@Override
	public
	ServiceTypeRec findRequired (
			@NonNull Long serviceTypeId) {

		return get (
			ServiceTypeRec.class,
			(int) (long) serviceTypeId);

	}

}
