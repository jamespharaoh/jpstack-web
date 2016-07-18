package wbs.platform.priv.hibernate;

import java.util.List;

import lombok.NonNull;

import wbs.framework.hibernate.HibernateDao;
import wbs.platform.priv.model.PrivTypeDao;
import wbs.platform.priv.model.PrivTypeRec;

public
class PrivTypeDaoHibernate
	extends HibernateDao
	implements PrivTypeDao {

	@Override
	public
	List<PrivTypeRec> findAll () {

		return findMany (
			"findAll ()",
			PrivTypeRec.class,

			createCriteria (
				PrivTypeRec.class)

		);

	}

	@Override
	public
	PrivTypeRec findRequired (
			@NonNull Long privTypeId) {

		return get (
			PrivTypeRec.class,
			(int) (long) privTypeId);

	}

}
